// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package ca.qc.banq.gia.authentication.helpers;

import ca.qc.banq.gia.authentication.config.AzureActiveDirectoryConfig;
import ca.qc.banq.gia.authentication.models.TokenResponse;
import ca.qc.banq.gia.authentication.models.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.aad.msal4j.*;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.openid.connect.sdk.AuthenticationErrorResponse;
import com.nimbusds.openid.connect.sdk.AuthenticationResponse;
import com.nimbusds.openid.connect.sdk.AuthenticationResponseParser;
import com.nimbusds.openid.connect.sdk.AuthenticationSuccessResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.naming.ServiceUnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static ca.qc.banq.gia.authentication.helpers.HttpClientHelper.*;
import static ca.qc.banq.gia.authentication.helpers.SessionManagementHelper.FAILED_TO_VALIDATE_MESSAGE;
import static ca.qc.banq.gia.authentication.helpers.SessionManagementHelper.validateState;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.replace;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;

/**
 * Helpers for acquiring authorization codes and tokens from AAD
 *
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-12
 */
@Getter
@Component
@RequiredArgsConstructor
public class AuthHelperAAD {

    public static final String PRINCIPAL_SESSION_NAME = "principal";
    public static final String TOKEN_CACHE_SESSION_ATTRIBUTE = "token_cache";

    private final AzureActiveDirectoryConfig azureActiveDirectoryConfig;

    private final RestTemplate restTemplate;

    @Value("${server.host}")
    private String serverHost;

    @Value("${server.servlet.context-path}")
    private String servletPath;

    private AppPayload app;

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
        StateData stateData = validateState(httpRequest.getSession(), params.get(SessionManagementHelper.STATE).get(0));

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
        IAuthenticationResult result = SessionManagementHelper.getAuthSessionObject(httpRequest);
        IConfidentialClientApplication app = createClientApplication();
        Object tokenCache = httpRequest.getSession().getAttribute("token_cache");
        if (nonNull(tokenCache)) app.tokenCache().deserialize(tokenCache.toString());
        SilentParameters parameters = SilentParameters.builder(Collections.singleton(azureActiveDirectoryConfig.getScope()), result.account()).build();
        CompletableFuture<IAuthenticationResult> future = app.acquireTokenSilently(parameters);
        IAuthenticationResult updatedResult = future.get();
        SessionManagementHelper.storeTokenCacheInSession(httpRequest, app.tokenCache().serialize());
        return updatedResult;
    }

    private void validateNonce(StateData stateData, String nonce) throws Exception {
        if (StringUtils.isEmpty(nonce) || !nonce.equals(stateData.nonce())) {
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
        PublicClientApplication pca = PublicClientApplication.builder(app.getClientId()).authority(azureActiveDirectoryConfig.getAuthority()).build();
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
                authority(azureActiveDirectoryConfig.getAuthority()).
                build();
    }

    private static boolean isAuthenticationSuccessful(AuthenticationResponse authResponse) {
        return authResponse instanceof AuthenticationSuccessResponse;
    }

    public String getRedirectUriSignIn() {
        return app.getRedirectApp();
    }

    public String getRedirectUriGraph() {
        return azureActiveDirectoryConfig.getRedirectUriGraph();
    }

    public String getMsGraphEndpointHost() {
        return azureActiveDirectoryConfig.getMsGraphEndpointHost();
    }

    /**
     * Recupere les infos de l'utilisateur connecte a partir de l'API Microsoft Graph
     */
    public UserInfo getADUserInfos(String token) {
        return callRestAPI(azureActiveDirectoryConfig.getMsGraphEndpointHost() + "v1.0/me", GET, null, UserInfo.class, null, buildHeaders(token));
    }

    /**
     * Recupere le token d'acces a GraphAPI
     */
    public TokenResponse getAccessToken(AppPayload app) {
        // Requete
        GetTokenRequestPayload req = new GetTokenRequestPayload(GRANT_TYPE_CREDENTIAL,
                app.getCertSecretValue(),
                app.getClientId(), azureActiveDirectoryConfig.getMsGraphScope());

        // Initialisation du header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_FORM_URLENCODED);

        // Construction du body de la requete
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", req.grant_type());
        map.add("client_secret", req.client_secret());
        map.add("client_id", req.client_id());
        map.add("scope", req.scope());
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

        // Execution du service rest de recuperation d'un token d'acces a GraphAPI
        ResponseEntity<TokenResponse> response = restTemplate.exchange(azureActiveDirectoryConfig.getAccessGraphTokenUri(),
                POST, entity, TokenResponse.class);

        // Retourne le token
        return response.getBody();
    }

    /**
     * Cree un nouvel utilisateur dans Azure AD/B2C
     */
    public UserInfo createUser(TokenResponse token, UserRequestPayload request) {
        return callRestAPI(azureActiveDirectoryConfig.getMsGraphUsersEndpoint(),
                POST, null, UserInfo.class, request, buildHeaders(token.access_token()));
    }

    public void editUser(TokenResponse token, EditB2CUserRequestPayload request) throws Exception {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPatch httpPatch = new HttpPatch(azureActiveDirectoryConfig.getMsGraphUsersEndpoint().concat("/" + request.getId()));
        System.err.println("Url edit user = " + httpPatch.getURI());
        org.apache.http.HttpEntity httpEntity = new StringEntity(new ObjectMapper().writeValueAsString(request));
        httpPatch.setHeader("Content-Type", "application/json");
        httpPatch.setHeader("Authorization", "Bearer " + token.access_token());
        httpPatch.setEntity(httpEntity);
        HttpResponse resp = httpClient.execute(httpPatch);
        System.err.println("User updated. StatusLine = " + resp.getStatusLine().getStatusCode() + " - " + resp.getStatusLine().getReasonPhrase());
    }

    /**
     * Ajoute un utilisateur a un groupe dans Azure AD
     *
     * @param uid id utilisateur ou userPrincipalName dans AD/B2C
     */
    public void addUserTGroup(TokenResponse token, String uid, String groupId) {
        callRestAPI(replace(azureActiveDirectoryConfig.getMsGraphAddUserToGroupEndpoint(), "$groupid", groupId),
                POST,
                null,
                Void.class,
                new AddUserToGroupRequestPayload("https://graph.microsoft.com/v1.0/directoryObjects/" + uid).getJsonData(),
                buildHeaders(token.access_token()));
    }

    /**
     * Assigne un utilisateur a une application
     */
    public void assignUserToApp(TokenResponse token, String uid, String appId) {
        // Recherche de l'application dans azureAD a partir de son ClientIdl
        FindAppByNameResponsePayload resp = callRestAPI(replace(FIND_APP_BYID_REQUEST_URL, "$appId", appId), GET, null, FindAppByNameResponsePayload.class, null, buildHeaders(token.access_token()));
        if (resp == null || resp.value() == null || resp.value().isEmpty()) return;

        // Recuperation de l'id de l'application
        String id = resp.value().get(0).id();

        // Affectation de l'utilisateur a l'application
        callRestAPI(replace(ASSIGN_USERTOAPP_REQUEST_URL, "$id", id),
                POST,
                null,
                AssignAppToUserResponsePayload.class,
                new AssignAppToUserRequestPayload(uid, id),
                buildHeaders(token.access_token()));
    }

    /**
     * Recupere les infos d'un utilisateur Azure AD/B2C
     *
     * @param uid id utilisateur ou userPrincipalName dans AD/B2C
     */
    public UserInfo getB2CUserInfos(TokenResponse token, String uid) {
        return callRestAPI(azureActiveDirectoryConfig.getMsGraphUsersEndpoint() + "/" + uid,
                GET,
                null,
                UserInfo.class,
                null,
                buildHeaders(token.access_token()));
    }

    /**
     * Recupere les identities d'un utilisateur
     *
     * @param uid id utilisateur Azure B2C ou userPrincipalName
     */
    public GetIdentitiesResponse getUserIdentities(TokenResponse token, String uid) {
        return callRestAPI(azureActiveDirectoryConfig.getMsGraphUsersEndpoint() + "/" + uid + "/identities",
                GET,
                null,
                GetIdentitiesResponse.class,
                null,
                buildHeaders(token.access_token()));
    }

    /**
     * Modifie les identifiants de connexion d'un utilisateur dans Azure B2C
     */
    public void editUserIdentities(TokenResponse token, String uid, GetIdentitiesResponse request) {
        callRestAPI(azureActiveDirectoryConfig.getMsGraphUsersEndpoint() + "/" + uid + "/identities",
                PUT,
                null,
                void.class,
                request,
                buildHeaders(token.access_token()));
    }

    /**
     * Genere un entete http avec token d'authentification
     */
    private HttpHeaders buildHeaders(String token) {

        // Initialisation du Header
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        // Ajout du token d'acces
        if (isNotEmpty(token)) requestHeaders.setBearerAuth(token);
        return requestHeaders;
    }

    public String getGIAUrlPath() {
        return serverHost.concat(servletPath);
    }
}
