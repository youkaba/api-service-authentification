// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package ca.qc.banq.gia.authentication.filter;

import ca.qc.banq.gia.authentication.exceptions.GIAException;
import ca.qc.banq.gia.authentication.helpers.*;
import ca.qc.banq.gia.authentication.models.*;
import ca.qc.banq.gia.authentication.servicesmetier.GiaBackOfficeService;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.MsalException;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.openid.connect.sdk.AuthenticationErrorResponse;
import com.nimbusds.openid.connect.sdk.AuthenticationResponse;
import com.nimbusds.openid.connect.sdk.AuthenticationResponseParser;
import com.nimbusds.openid.connect.sdk.AuthenticationSuccessResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Filtre de requetes pour l'authentification B2C
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-12
 */
@Slf4j
@Getter
@Component
@RequiredArgsConstructor
public class AuthFilterB2C {

    private static final String STATE = "state";
    private static final String FAILED_TO_VALIDATE_MESSAGE = "Failed to validate data received from Authorization service - ";

    private final AuthHelperB2C authHelper;

    private final AuthHelperAAD authHelperAAD;

    private final GiaBackOfficeService business;

	@Value("${server.host}")
	String serverHost;

    public void doFilter(ServletRequest request, ServletResponse response) throws Throwable {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            try {
                String currentUri = serverHost.concat(httpRequest.getRequestURI());  // httpRequest.getRequestURL().toString();
                String queryStr = httpRequest.getQueryString();
                String fullUrl = currentUri + (queryStr != null ? "?" + queryStr : "");

                // check if user has a AuthData in the session
                if (!authHelper.isAuthenticated(httpRequest)) {
                    if(authHelper.containsAuthenticationCode(httpRequest)){
                        // response should have authentication code, which will be used to acquire access token
                        processAuthenticationCodeRedirect(httpRequest, currentUri, fullUrl);
                        CookieHelper.removeStateNonceCookies(httpResponse);
                    } else {
                        // not authenticated, redirecting to login.microsoft.com so user can authenticate
                        sendAuthRedirect(authHelper.getConfiguration().getSignUpSignInAuthority(authHelper.getApp().getPolicySignUpSignIn()), httpRequest, httpResponse);
                        return;
                    }
                }
                if (isAccessTokenExpired(httpRequest)) {
                	authHelper.updateAuthDataUsingSilentFlow(httpRequest);
                }
                if (authHelper.isAuthenticated(httpRequest) && !isAccessTokenExpired(httpRequest)) {
                	IAuthenticationResult auth = authHelper.getAuthSessionObject(httpRequest);
                	Map<String, Object> claims = JWTParser.parse(auth.idToken()).getJWTClaimsSet().getClaims();
                	String uid = claims.get(HttpClientHelper.CLAIM_USERID).toString();
                	String appid = StringUtils.remove( StringUtils.remove(claims.get("aud").toString(), '['), ']') ;
                	AppPayload app = business.findByClientId(appid);
                	
                	// Si le claim ne contient pas les attributs requis on leve une exception
                	if(uid == null || app == null) {
                		httpResponse.setStatus(500);
            			httpRequest.setAttribute("error", "unable to find " + HttpClientHelper.CLAIM_USERID + " or 'aud' property within the claim");
            			httpRequest.getRequestDispatcher("/error").forward(httpRequest, httpResponse);
            			return;
                	}

            	  	// Obtention du Token d'acces a GraphAPI
            	  	TokenResponse token = authHelperAAD.getAccessToken(app);
                	
            	  	// Recuperation des identities de l'usager 
            	  	GetIdentitiesResponse identities = authHelperAAD.getUserIdentities(token, uid);
            	  	
            	  	// Recherche de l'identite de type "userName"
            	  	List<IdentityPayload> idsUserName = identities.getValue().stream().filter(ip -> ip.getSignInType().equals(SignInType.USERNAME.getValue())).collect(Collectors.toList());
            	  	if( !idsUserName.isEmpty() ) {
            	  		// Si on a trouve une identite de type "userName", c'est elle qu'on recupere comme identifiant de l'usager
            	  		uid = idsUserName.stream().findFirst().orElse(null).getIssuerAssignedId();
            	  	} else {
            	  		// Si on n'a pas trouve une identite de type "userName", on recupere celle de type "emailAddress" comme identifiant 
            	  		idsUserName = identities.getValue().stream().filter(ip -> ip.getSignInType().equals(SignInType.EMAIL.getValue())).collect(Collectors.toList());
            	  		if( !idsUserName.isEmpty() ) uid = idsUserName.stream().findFirst().orElse(null).getIssuerAssignedId();
            	  	}
                	
            	  	// Redirection vers la page d'accueil de l'application
                	httpResponse.sendRedirect(SessionManagementHelper.buildRedirectAppHomeUrl(auth, uid, app, authHelperAAD.getGIAUrlPath()));
                }
            } catch (MsalException authException) {
                // something went wrong (like expiration or revocation of token)
                // we should invalidate AuthData stored in session and redirect to Authorization server
                authHelper.removePrincipalFromSession(httpRequest);
                sendAuthRedirect(authHelper.getConfiguration().getSignUpSignInAuthority(authHelper.getApp().getPolicySignUpSignIn()), httpRequest, httpResponse);
                authException.printStackTrace();
            }
        }
        //chain.doFilter(request, response);
    }

    private boolean isAccessTokenExpired(HttpServletRequest httpRequest) {
        IAuthenticationResult result = authHelper.getAuthSessionObject(httpRequest);
        return result.expiresOnDate().before(new Date());
    }

    private void processAuthenticationCodeRedirect(HttpServletRequest httpRequest, String currentUri, String fullUrl) throws Throwable {

        Map<String, List<String>> params = new HashMap<>();
        for (String key : httpRequest.getParameterMap().keySet()) {
            params.put(key, Collections.singletonList(httpRequest.getParameterMap().get(key)[0]));
        }
        
        // validate that state in response equals to state in request
        validateState(CookieHelper.getCookie(httpRequest, CookieHelper.MSAL_WEB_APP_STATE_COOKIE), params.get(STATE).get(0));

        AuthenticationResponse authResponse = AuthenticationResponseParser.parse(new URI(fullUrl), params);
        if (authHelper.isAuthenticationSuccessful(authResponse)) {
            AuthenticationSuccessResponse oidcResponse = (AuthenticationSuccessResponse) authResponse;
            // validate that OIDC Auth Response matches Code Flow (contains only requested artifacts)
            validateAuthRespMatchesAuthCodeFlow(oidcResponse);

            IAuthenticationResult result = authHelper.getAuthResultByAuthCode(httpRequest, oidcResponse.getAuthorizationCode(), currentUri, Collections.singleton(authHelper.getApp().getApiScope()));

            // validate nonce to prevent reply attacks (code maybe substituted to one with broader access)
            validateNonce(CookieHelper.getCookie(httpRequest, CookieHelper.MSAL_WEB_APP_NONCE_COOKIE), getNonceClaimValueFromIdToken(result.idToken()));

            authHelper.setSessionPrincipal(httpRequest, result);
        } else {
            AuthenticationErrorResponse oidcResponse = (AuthenticationErrorResponse) authResponse;
            throw new GIAException(String.format("Request for auth code failed: %s - %s", oidcResponse.getErrorObject().getCode(), oidcResponse.getErrorObject().getDescription()));
        }
    }

    private void sendAuthRedirect(String authoriy, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        // state parameter to validate response from Authorization server and nonce parameter to validate idToken
        String state = UUID.randomUUID().toString();
        String nonce = UUID.randomUUID().toString();
        CookieHelper.setStateNonceCookies(httpRequest, httpResponse, state, nonce);
        httpResponse.setStatus(302);
        String redirectUrl = getRedirectUrl(authoriy, httpRequest.getParameter("claims"), state, nonce);
        httpResponse.sendRedirect(redirectUrl);
    }

    private String getNonceClaimValueFromIdToken(String idToken) throws ParseException {
        return (String) JWTParser.parse(idToken).getJWTClaimsSet().getClaim("nonce");
    }

    private void validateState(List<String> cookieValues, String state) throws Exception {
        if (StringUtils.isEmpty(state) || !cookieValues.contains(state)) {
            throw new Exception(FAILED_TO_VALIDATE_MESSAGE + "could not validate state");
        }
    }

    private void validateNonce(List<String> cookieValues, String nonce) throws Exception {
        if (StringUtils.isEmpty(nonce) || !cookieValues.contains(nonce)) {
            throw new Exception(FAILED_TO_VALIDATE_MESSAGE + "could not validate nonce");
        }
    }

    private void validateAuthRespMatchesAuthCodeFlow(AuthenticationSuccessResponse oidcResponse) throws Exception {
        if (oidcResponse.getIDToken() != null || oidcResponse.getAccessToken() != null || oidcResponse.getAuthorizationCode() == null) {
            throw new Exception(FAILED_TO_VALIDATE_MESSAGE + "unexpected set of artifacts received");
        }
    }

    private String getRedirectUrl(String authority, String claims, String state, String nonce) {

        return authority.replace("/tfp", "") + "oauth2/v2.0/authorize?" +
                "response_type=code&" +
                "response_mode=query&" +
                "redirect_uri=" + URLEncoder.encode(authHelper.getApp().getRedirectApp(), UTF_8) +
                "&client_id=" + authHelper.getApp().getClientId() +
                "&scope=" + URLEncoder.encode("openid offline_access profile " +
                authHelper.getApp().getApiScope(), UTF_8) +
                (StringUtils.isEmpty(claims) ? "" : "&claims=" + claims) +
                "&prompt=select_account" +
                "&state=" + state
                + "&nonce=" + nonce;
    }
	
}
