// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package ca.qc.banq.gia.authentication.helpers;

import static ca.qc.banq.gia.authentication.helpers.SessionManagementHelper.FAILED_TO_VALIDATE_MESSAGE;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.naming.ServiceUnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.microsoft.aad.msal4j.AuthorizationCodeParameters;
import com.microsoft.aad.msal4j.AuthorizationRequestUrlParameters;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IConfidentialClientApplication;
import com.microsoft.aad.msal4j.Prompt;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.ResponseMode;
import com.microsoft.aad.msal4j.SilentParameters;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.openid.connect.sdk.AuthenticationErrorResponse;
import com.nimbusds.openid.connect.sdk.AuthenticationResponse;
import com.nimbusds.openid.connect.sdk.AuthenticationResponseParser;
import com.nimbusds.openid.connect.sdk.AuthenticationSuccessResponse;

import ca.qc.banq.gia.authentication.config.AADConfig;
import ca.qc.banq.gia.authentication.models.AddUserToGroupRequestPayload;
import ca.qc.banq.gia.authentication.models.AppPayload;
import ca.qc.banq.gia.authentication.models.AssignAppToUserRequestPayload;
import ca.qc.banq.gia.authentication.models.AssignAppToUserResponsePayload;
import ca.qc.banq.gia.authentication.models.FindAppByNameResponsePayload;
import ca.qc.banq.gia.authentication.models.GetIdentitiesResponse;
import ca.qc.banq.gia.authentication.models.GetTokenRequestPayload;
import ca.qc.banq.gia.authentication.models.StateData;
import ca.qc.banq.gia.authentication.models.TokenResponse;
import ca.qc.banq.gia.authentication.models.UserInfo;
import ca.qc.banq.gia.authentication.models.UserRequestPayload;
import lombok.Getter;

/**
 * Helpers for acquiring authorization codes and tokens from AAD
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-12
 */
@Getter
@Component
public class AuthHelperAAD {

    public static final String PRINCIPAL_SESSION_NAME = "principal";
    public static final String TOKEN_CACHE_SESSION_ATTRIBUTE = "token_cache";

	@Value("${server.host}")
	String serverHost;
	
	@Value("${server.servlet.context-path}")
	String servletPath;
	
    @Autowired
    AADConfig configuration;
    
    AppPayload app;

    //@PostConstruct
    public void init(AppPayload app) {
    	this.app = app;
    }

    public void processAuthenticationCodeRedirect(HttpServletRequest httpRequest, String currentUri, String fullUrl) throws Throwable {

        Map<String, List<String>> params = new HashMap<>();
        for (String key : httpRequest.getParameterMap().keySet()) {
            params.put(key, Collections.singletonList(httpRequest.getParameterMap().get(key)[0]));
        }
        // validate that state in response equals to state in request
        StateData stateData = SessionManagementHelper.validateState(httpRequest.getSession(), params.get(SessionManagementHelper.STATE).get(0));

        AuthenticationResponse authResponse = AuthenticationResponseParser.parse(new URI(fullUrl), params);
        if (AuthHelperAAD.isAuthenticationSuccessful(authResponse)) {
            AuthenticationSuccessResponse oidcResponse = (AuthenticationSuccessResponse) authResponse;
            
            // validate that OIDC Auth Response matches Code Flow (contains only requested artifacts)
            validateAuthRespMatchesAuthCodeFlow(oidcResponse);
            
            IAuthenticationResult result = getAuthResultByAuthCode(httpRequest, oidcResponse.getAuthorizationCode(), currentUri);
            
            // validate nonce to prevent reply attacks (code maybe substituted to one with broader access)
            validateNonce(stateData, getNonceClaimValueFromIdToken(result.idToken()));

            SessionManagementHelper.setSessionPrincipal(httpRequest, result);
        } else {
            AuthenticationErrorResponse oidcResponse = (AuthenticationErrorResponse) authResponse;
            throw new Exception(String.format("Request for auth code failed: %s - %s", oidcResponse.getErrorObject().getCode(), oidcResponse.getErrorObject().getDescription()));
        }
    }

    public IAuthenticationResult getAuthResultBySilentFlow(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Throwable {
        IAuthenticationResult result =  SessionManagementHelper.getAuthSessionObject(httpRequest);
        IConfidentialClientApplication app = createClientApplication();
        Object tokenCache = httpRequest.getSession().getAttribute("token_cache");
        if (tokenCache != null) app.tokenCache().deserialize(tokenCache.toString());
        SilentParameters parameters = SilentParameters.builder(Collections.singleton("User.Read"), result.account()).build();
        CompletableFuture<IAuthenticationResult> future = app.acquireTokenSilently(parameters);
        IAuthenticationResult updatedResult = future.get();
        SessionManagementHelper.storeTokenCacheInSession(httpRequest, app.tokenCache().serialize());
        return updatedResult;
    }

    private void validateNonce(StateData stateData, String nonce) throws Exception {
        if (StringUtils.isEmpty(nonce) || !nonce.equals(stateData.getNonce())) {
            throw new Exception(FAILED_TO_VALIDATE_MESSAGE + "could not validate nonce");
        }
    }

    private String getNonceClaimValueFromIdToken(String idToken) throws ParseException {
        return (String) JWTParser.parse(idToken).getJWTClaimsSet().getClaim("nonce");
    }

    private void validateAuthRespMatchesAuthCodeFlow(AuthenticationSuccessResponse oidcResponse) throws Exception {
        if (oidcResponse.getIDToken() != null || oidcResponse.getAccessToken() != null || oidcResponse.getAuthorizationCode() == null) {
            throw new Exception(FAILED_TO_VALIDATE_MESSAGE + "unexpected set of artifacts received");
        }
    }

    public void sendAuthRedirect(HttpServletRequest httpRequest, HttpServletResponse httpResponse, String scope, String redirectURL) throws IOException {

        // state parameter to validate response from Authorization server and nonce parameter to validate idToken
        String state = UUID.randomUUID().toString();
        String nonce = UUID.randomUUID().toString();

        SessionManagementHelper.storeStateAndNonceInSession(httpRequest.getSession(), state, nonce);

        httpResponse.setStatus(302);
        String authorizationCodeUrl = getAuthorizationCodeUrl(httpRequest.getParameter("claims"), scope, redirectURL, state, nonce);
        httpResponse.sendRedirect(authorizationCodeUrl);
    }

    public String getAuthorizationCodeUrl(String claims, String scope, String registeredRedirectURL, String state, String nonce) throws MalformedURLException {

        String updatedScopes = scope == null ? "" : scope;
        PublicClientApplication pca = PublicClientApplication.builder(app.getClientId()).authority(configuration.getAuthority()).build();
        AuthorizationRequestUrlParameters parameters = AuthorizationRequestUrlParameters.builder(registeredRedirectURL, Collections.singleton(updatedScopes))
                        .responseMode(ResponseMode.QUERY).prompt(Prompt.SELECT_ACCOUNT).state(state).nonce(nonce).claimsChallenge(claims).build();
        return pca.getAuthorizationRequestUrl(parameters).toString();
    }

    private IAuthenticationResult getAuthResultByAuthCode(HttpServletRequest httpServletRequest, AuthorizationCode authorizationCode, String currentUri) throws Throwable {
        IAuthenticationResult result;
        ConfidentialClientApplication app;
        try {
            app = createClientApplication();
            String authCode = authorizationCode.getValue();
            AuthorizationCodeParameters parameters = AuthorizationCodeParameters.builder(authCode, new URI(currentUri)).build();
            Future<IAuthenticationResult> future = app.acquireToken(parameters);
            result = future.get();
        } catch (ExecutionException e) {
            throw e.getCause();
        }
        if (result == null) throw new ServiceUnavailableException("authentication result was null");
        SessionManagementHelper.storeTokenCacheInSession(httpServletRequest, app.tokenCache().serialize());
        return result;
    }

    private ConfidentialClientApplication createClientApplication() throws MalformedURLException {
        return ConfidentialClientApplication.builder(app.getClientId(), ClientCredentialFactory.createFromSecret(app.getCertSecretValue())).
                authority(configuration.getAuthority()).
                build();
    }

    private static boolean isAuthenticationSuccessful(AuthenticationResponse authResponse) {
        return authResponse instanceof AuthenticationSuccessResponse;
    }

    public String getRedirectUriSignIn() {
        return app.getRedirectApp();
    }

    public String getRedirectUriGraph() {
        return configuration.getRedirectUriGraph();
    }

    public String getMsGraphEndpointHost(){
        return configuration.getMsGraphEndpointHost();
    }

	/**
	 * Recupere les infos de l'utilisateur connecte a partir de l'API Microsoft Graph
	 * @param token
	 * @return
	 */
	public UserInfo getADUserInfos(String token) {
    	return HttpClientHelper.callRestAPI(configuration.getMsGraphEndpointHost() + "v1.0/me", HttpMethod.GET, null, UserInfo.class, null, buildHeaders(token));
	}
    
	/**
	 * Recupere le token d'acces a GraphAPI
	 * @param app
	 * @return
	 */
    public TokenResponse getAccessToken(AppPayload app) {
    	// Requete
	  	GetTokenRequestPayload req = new GetTokenRequestPayload(HttpClientHelper.GRANT_TYPE_CREDENTIAL, app.getCertSecretValue(), app.getClientId(), configuration.getMsGraphScope());
	  	RestTemplate restTemplate = new RestTemplate();
	  	
	  	// Initialisation du header
	  	HttpHeaders headers = new HttpHeaders();
	  	headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
	  	
	  	// Construction du body de la requete
	  	MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
	  	map.add("grant_type",req.getGrant_type());
	  	map.add("client_secret",req.getClient_secret());
	  	map.add("client_id",req.getClient_id());
	  	map.add("scope",req.getScope());
	  	HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
	  	
	  	// Execution du service rest de recuperation d'un token d'acces a GraphAPI
	  	ResponseEntity<TokenResponse> response = restTemplate.exchange(configuration.getAccessGraphTokenUri(), HttpMethod.POST, entity, TokenResponse.class);
	  	
	  	// Retourne le token
	  	return response.getBody();
    }

    /**
     * Cree un nouvel utilisateur dans Azure AD/B2C
     * @param token
     * @param request
     * @return
     */
    public UserInfo createUser(TokenResponse token, UserRequestPayload request) {
	  	return HttpClientHelper.callRestAPI(configuration.getMsGraphUsersEndpoint(), HttpMethod.POST, null, UserInfo.class, request, buildHeaders(token.getAccess_token()) );
    }

    /**
     * Ajoute un utilisateur a un groupe dans Azure AD
     * @param token
     * @param uid id utilisateur ou userPrincipalName dans AD/B2C
     * @param groupId
     */
    public void addUserTGroup(TokenResponse token, String uid, String groupId) {
    	HttpClientHelper.callRestAPI(StringUtils.replace(configuration.getMsGraphAddUserToGroupEndpoint(), "$groupid", groupId) , HttpMethod.POST, null, Void.class, new AddUserToGroupRequestPayload(uid).getJsonData(), buildHeaders(token.getAccess_token()) );
    }

    /**
     * Assigne un utilisateur a une application
     * @param token
     * @param uid
     * @param appId
     */
    public void assignUserToApp(TokenResponse token, String uid, String appId) {
    	// Recherche de l'application dans azureAD a partir de son ClientIdl
    	FindAppByNameResponsePayload resp = HttpClientHelper.callRestAPI(StringUtils.replace(HttpClientHelper.FIND_APP_BYID_REQUEST_URL, "$appId", appId) , HttpMethod.GET, null, FindAppByNameResponsePayload.class, null, buildHeaders(token.getAccess_token()) );
    	if(resp == null || resp.getValue() == null || resp.getValue().isEmpty()) return;
    	
    	// Recuperation de l'id de l'application
    	String id = resp.getValue().get(0).getId();
    	
    	// Affectation de l'utilisateur a l'application
    	HttpClientHelper.callRestAPI(StringUtils.replace(HttpClientHelper.ASSIGN_USERTOAPP_REQUEST_URL, "$id", id ) , HttpMethod.POST, null, AssignAppToUserResponsePayload.class, new AssignAppToUserRequestPayload(uid, id), buildHeaders(token.getAccess_token()) );
    }

    /**
     * Recupere les infos d'un utilisateur Azure AD/B2C
     * @param token
     * @param uid id utilisateur ou userPrincipalName dans AD/B2C
     * @return
     */
    public UserInfo getB2CUserInfos(TokenResponse token, String uid) {
	  	return HttpClientHelper.callRestAPI(configuration.getMsGraphUsersEndpoint() + "/" + uid, HttpMethod.GET, null, UserInfo.class, null, buildHeaders(token.getAccess_token()) );
    }
	
    /**
     * Recupere les identities d'un utilisateur
     * @param token
     * @param uid id utilisateur Azure B2C ou userPrincipalName
     * @return
     */
    public GetIdentitiesResponse getUserIdentities(TokenResponse token, String uid) {
	  	return HttpClientHelper.callRestAPI(configuration.getMsGraphUsersEndpoint() + "/" + uid + "/identities", HttpMethod.GET, null, GetIdentitiesResponse.class, null, buildHeaders(token.getAccess_token()) );
    }
    
    /**
     * Genere un entete http avec token d'authentification
     * @param token
     * @return
     */
    private HttpHeaders buildHeaders(String token) {

	  	// Initialisation du Header
	  	HttpHeaders requestHeaders = new HttpHeaders();
	  	requestHeaders.setContentType(MediaType.APPLICATION_JSON);
	  	
	  	// Ajout du token d'acces
	  	if(token != null && !token.isEmpty()) requestHeaders.setBearerAuth(token);
	  	return requestHeaders;
    }
    
    public String getGIAUrlPath() {
    	return serverHost.concat(servletPath);
    }
	
}
