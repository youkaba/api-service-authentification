// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package ca.qc.banq.gia.authentication.filter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.MsalException;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.openid.connect.sdk.AuthenticationErrorResponse;
import com.nimbusds.openid.connect.sdk.AuthenticationResponse;
import com.nimbusds.openid.connect.sdk.AuthenticationResponseParser;
import com.nimbusds.openid.connect.sdk.AuthenticationSuccessResponse;

import ca.qc.banq.gia.authentication.helpers.AuthHelperAAD;
import ca.qc.banq.gia.authentication.helpers.AuthHelperB2C;
import ca.qc.banq.gia.authentication.helpers.CookieHelper;
import ca.qc.banq.gia.authentication.helpers.HttpClientHelper;
import ca.qc.banq.gia.authentication.models.TokenResponse;
import ca.qc.banq.gia.authentication.models.UserInfo;
//import ca.qc.banq.gia.authentication.helpers.CookieHelper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Filtre de requetes pour l'authentification B2C
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-12
 */
@Slf4j
@Getter
@Component
public class AuthFilterB2C {

    private static final String STATE = "state";
    private static final String FAILED_TO_VALIDATE_MESSAGE = "Failed to validate data received from Authorization service - ";

    @Autowired
    AuthHelperB2C authHelper;

    @Autowired
    AuthHelperAAD authHelperAAD;

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            try {
                String currentUri = httpRequest.getRequestURL().toString();
                //String path = httpRequest.getServletPath();
                String queryStr = httpRequest.getQueryString();
                String fullUrl = currentUri + (queryStr != null ? "?" + queryStr : "");

                // check if user has a AuthData in the session
                if (!authHelper.isAuthenticated(httpRequest)) {
                    if(authHelper.containsAuthenticationCode(httpRequest)){
                        // response should have authentication code, which will be used to acquire access token
                        processAuthenticationCodeRedirect(httpRequest, httpResponse, currentUri, fullUrl);

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
                	if(claims.get(HttpClientHelper.BAnQ_CUSTOM_USERID) == null) {
                		httpResponse.setStatus(500);
            			httpRequest.setAttribute("error", "unable to find " + HttpClientHelper.BAnQ_CUSTOM_USERID + " property within the claim");
            			httpRequest.getRequestDispatcher("/error").forward(httpRequest, httpResponse);
            			return;
                	}
                	
                	try {
                		log.info("authorization_code = " + httpRequest.getParameter("code"));
                		TokenResponse token = authHelperAAD.getToken(httpRequest, authHelper.getApp());
                		log.info("Get_access_Token = " + token.toString());
                		List users = authHelperAAD.getAllUsers(token.getAccess_token());
                		log.info(users.toString());
                	}catch(Exception e) {log.error(e.getMessage());}
                	
                	String uid = claims.get(HttpClientHelper.BAnQ_CUSTOM_USERID).toString();
                	String query = "?" + HttpClientHelper.ACCESS_TOKEN + "=" + auth.accessToken() + "&" + HttpClientHelper.EXPDATE_SESSION_NAME + "=" + String.valueOf(auth.expiresOnDate().getTime()) + "&" + HttpClientHelper.UID_SESSION_NAME + "=" + uid + "&" + AuthFilter.APP_ID + "=" + authHelper.getApp().getClientId() + "&" + HttpClientHelper.SIGNIN_URL + "=" +  URLEncoder.encode(authHelper.getApp().getLoginURL(), "UTF-8") + "&" + HttpClientHelper.SIGNOUT_URL + "=" +  URLEncoder.encode(authHelper.getApp().getLogoutURL(), "UTF-8") ;
                    httpResponse.sendRedirect(authHelper.getApp().getHomeUrl().concat(query));
                    return;
                }
            } catch (MsalException authException) {
                // something went wrong (like expiration or revocation of token)
                // we should invalidate AuthData stored in session and redirect to Authorization server
                authHelper.removePrincipalFromSession(httpRequest);
                sendAuthRedirect(authHelper.getConfiguration().getSignUpSignInAuthority(authHelper.getApp().getPolicySignUpSignIn()), httpRequest, httpResponse);
                authException.printStackTrace();
                return;
            } catch (Throwable exc) {
                httpResponse.setStatus(500);
                request.setAttribute("error", exc.getMessage());
                request.getRequestDispatcher("/error").forward(request, response);
                exc.printStackTrace();
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private boolean isAccessTokenExpired(HttpServletRequest httpRequest) {
        IAuthenticationResult result = authHelper.getAuthSessionObject(httpRequest);
        return result.expiresOnDate().before(new Date());
    }

    private void processAuthenticationCodeRedirect(HttpServletRequest httpRequest, HttpServletResponse httpResponse, String currentUri, String fullUrl)
            throws Throwable {

        Map<String, List<String>> params = new HashMap<>();
        for (String key : httpRequest.getParameterMap().keySet()) {
            params.put(key, Collections.singletonList(httpRequest.getParameterMap().get(key)[0]));
        }
        System.err.println(params);
        // validate that state in response equals to state in request
        validateState(CookieHelper.getCookie(httpRequest, CookieHelper.MSAL_WEB_APP_STATE_COOKIE), params.get(STATE).get(0));

        AuthenticationResponse authResponse = AuthenticationResponseParser.parse(new URI(fullUrl), params);
        if (authHelper.isAuthenticationSuccessful(authResponse)) {
            AuthenticationSuccessResponse oidcResponse = (AuthenticationSuccessResponse) authResponse;
            // validate that OIDC Auth Response matches Code Flow (contains only requested artifacts)
            validateAuthRespMatchesAuthCodeFlow(oidcResponse);

            IAuthenticationResult result = authHelper.getAuthResultByAuthCode(
                    httpRequest,
                    oidcResponse.getAuthorizationCode(),
                    currentUri,
                    Collections.singleton(authHelper.getApp().getApiScope()));

            // validate nonce to prevent reply attacks (code maybe substituted to one with broader access)
            validateNonce(CookieHelper.getCookie(httpRequest, CookieHelper.MSAL_WEB_APP_NONCE_COOKIE),
                    getNonceClaimValueFromIdToken(result.idToken()));

            authHelper.setSessionPrincipal(httpRequest, result);
        } else {
            AuthenticationErrorResponse oidcResponse = (AuthenticationErrorResponse) authResponse;
            throw new Exception(String.format("Request for auth code failed: %s - %s",
                    oidcResponse.getErrorObject().getCode(),
                    oidcResponse.getErrorObject().getDescription()));
        }
    }

    void sendAuthRedirect(String authoriy, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
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
        if (StringUtils.isEmpty(state) || !cookieValues.contains(state)) { //state.equals(cookieValue)) {
        	System.err.println("state = " + state + " - cookieValues = " + cookieValues);
            throw new Exception(FAILED_TO_VALIDATE_MESSAGE + "could not validate state");
        }
    }

    private void validateNonce(List<String> cookieValues, String nonce) throws Exception {
        if (StringUtils.isEmpty(nonce) || !cookieValues.contains(nonce)) { //nonce.equals(cookieValue)) {
            throw new Exception(FAILED_TO_VALIDATE_MESSAGE + "could not validate nonce");
        }
    }

    private void validateAuthRespMatchesAuthCodeFlow(AuthenticationSuccessResponse oidcResponse) throws Exception {
        if (oidcResponse.getIDToken() != null || oidcResponse.getAccessToken() != null ||
                oidcResponse.getAuthorizationCode() == null) {
            throw new Exception(FAILED_TO_VALIDATE_MESSAGE + "unexpected set of artifacts received");
        }
    }

    private String getRedirectUrl(String authority, String claims, String state, String nonce)
            throws UnsupportedEncodingException {

        String redirectUrl = authority.replace("/tfp", "") + "oauth2/v2.0/authorize?" +
                "response_type=code&" +
                "response_mode=query&" +
                "redirect_uri=" + URLEncoder.encode(authHelper.getApp().getRedirectApp(), "UTF-8") +
                "&client_id=" + authHelper.getApp().getClientId() +
                "&scope=" + URLEncoder.encode("openid offline_access profile " +
                authHelper.getApp().getApiScope(), "UTF-8") +
                (StringUtils.isEmpty(claims) ? "" : "&claims=" + claims) +
                "&prompt=select_account" +
                "&state=" + state
                + "&nonce=" + nonce;

        return redirectUrl;
    }
	
}
