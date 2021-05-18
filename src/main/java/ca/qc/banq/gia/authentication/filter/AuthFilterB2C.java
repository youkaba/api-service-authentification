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

import ca.qc.banq.gia.authentication.helpers.AuthHelperB2C;
import ca.qc.banq.gia.authentication.helpers.CookieHelper;
//import ca.qc.banq.gia.authentication.helpers.CookieHelper;
import lombok.Getter;

/**
 * Filtre de requetes pour l'authentification B2C
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-12
 */
@Getter
@Component
public class AuthFilterB2C {

    private static final String STATE = "state";
    private static final String FAILED_TO_VALIDATE_MESSAGE = "Failed to validate data received from Authorization service - ";

    @Autowired
    AuthHelperB2C authHelper;
    
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

    private void processAuthenticationCodeRedirect(HttpServletRequest httpRequest, String currentUri, String fullUrl)
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
                "redirect_uri=" + URLEncoder.encode(authHelper.getApp().getHomeUrl(), "UTF-8") +
                "&client_id=" + authHelper.getApp().getClientId() +
                "&scope=" + URLEncoder.encode("openid offline_access profile " +
                authHelper.getApp().getApiScope(), "UTF-8") +
                (StringUtils.isEmpty(claims) ? "" : "&claims=" + claims) +
                "&prompt=select_account" +
                "&state=" + state
                + "&nonce=" + nonce;

        return redirectUrl;
    }
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*
	
    private static final String STATE = "state";
    private static final String FAILED_TO_VALIDATE_MESSAGE = "Failed to validate data received from Authorization service - ";

    @Autowired
    AuthHelperB2C authHelper;
    
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
                        processAuthenticationCodeRedirect(httpRequest, currentUri, fullUrl);
                        removeStateNonceCookies(httpResponse);
                    } else {
                        // not authenticated, redirecting to login.microsoft.com so user can authenticate
                        sendAuthRedirect(authHelper.getConfiguration().getSignUpSignInAuthority(), httpRequest, httpResponse);
                        return;
                    }
                }
                if (isAccessTokenExpired(httpRequest)) {
                    authHelper.updateAuthDataUsingSilentFlow(httpRequest);
                }
            } catch (MsalException authException) {
                // something went wrong (like expiration or revocation of token)
                // we should invalidate AuthData stored in session and redirect to Authorization server
                authHelper.removePrincipalFromSession(httpRequest);
                sendAuthRedirect(authHelper.getConfiguration().getSignUpSignInAuthority(), httpRequest, httpResponse);
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

    private void processAuthenticationCodeRedirect(HttpServletRequest httpRequest, String currentUri, String fullUrl) throws Throwable {

        Map<String, List<String>> params = new HashMap<>();
        for (String key : httpRequest.getParameterMap().keySet()) {
            params.put(key, Collections.singletonList(httpRequest.getParameterMap().get(key)[0]));
        }
        System.err.println(params);
        // validate that state in response equals to state in request
        validateState(getCookie(httpRequest, MSAL_WEB_APP_STATE_COOKIE), params.get(STATE).get(0));

        AuthenticationResponse authResponse = AuthenticationResponseParser.parse(new URI(fullUrl), params);
        if (authHelper.isAuthenticationSuccessful(authResponse)) {
            AuthenticationSuccessResponse oidcResponse = (AuthenticationSuccessResponse) authResponse;
            // validate that OIDC Auth Response matches Code Flow (contains only requested artifacts)
            validateAuthRespMatchesAuthCodeFlow(oidcResponse);

            IAuthenticationResult result = authHelper.getAuthResultByAuthCode(
                    httpRequest,
                    oidcResponse.getAuthorizationCode(),
                    currentUri,
                    Collections.singleton(authHelper.getApp().getClientId()));

            // validate nonce to prevent reply attacks (code maybe substituted to one with broader access)
            validateNonce(getCookie(httpRequest, MSAL_WEB_APP_NONCE_COOKIE),
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

        setStateNonceCookies(httpRequest, httpResponse, state, nonce);

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
                "redirect_uri=" + URLEncoder.encode(authHelper.getApp().getHomeUrl(), "UTF-8") +
                "&client_id=" + authHelper.getApp().getClientId() +
                "&scope=" + URLEncoder.encode("openid offline_access profile " +
                authHelper.getApp().getClientId(), "UTF-8") +
                (StringUtils.isEmpty(claims) ? "" : "&claims=" + claims) +
                "&prompt=select_account" +
                "&state=" + state
                + "&nonce=" + nonce;

        return redirectUrl;
    }
    
    
    
    static final String MSAL_WEB_APP_STATE_COOKIE = "msal_web_app_auth_state";
    static final String MSAL_WEB_APP_NONCE_COOKIE = "msal_web_app_auth_nonce";

    static void setStateNonceCookies
            (HttpServletRequest httpRequest, HttpServletResponse httpResponse, String state, String nonce){

        boolean userAgentSameSiteNoneAware =
                isUserAgentAwareOfSameSiteNone(httpRequest.getHeader("User-Agent"));

        String sameSiteCookieAttribute = userAgentSameSiteNoneAware ? "; SameSite=none" : "";

        httpResponse.addHeader("Set-Cookie",
                MSAL_WEB_APP_STATE_COOKIE + "=" + state + "; secure; HttpOnly" + sameSiteCookieAttribute);

        httpResponse.addHeader("Set-Cookie",
                MSAL_WEB_APP_NONCE_COOKIE + "=" + nonce + "; secure; HttpOnly" + sameSiteCookieAttribute);
    }

    static void removeStateNonceCookies(HttpServletResponse httpResponse){

        Cookie stateCookie = new Cookie(MSAL_WEB_APP_STATE_COOKIE, "");
        stateCookie.setMaxAge(0);

        httpResponse.addCookie(stateCookie);

        Cookie nonceCookie = new Cookie(MSAL_WEB_APP_NONCE_COOKIE, "");
        nonceCookie.setMaxAge(0);

        httpResponse.addCookie(nonceCookie);
    }

    static List<String> getCookie(HttpServletRequest httpRequest, String cookieName){
    	return Arrays.asList(httpRequest.getCookies()).stream().map(cookie -> cookie.getName().equals(cookieName) ? cookie.getValue() : null ).filter(c -> c != null).collect(Collectors.toList());
        
    }

    static boolean isUserAgentAwareOfSameSiteNone(String userAgent){

        // Cover all iOS based browsers here. This includes:
        // - Safari on iOS 12 for iPhone, iPod Touch, iPad
        // - WkWebview on iOS 12 for iPhone, iPod Touch, iPad
        // - Chrome on iOS 12 for iPhone, iPod Touch, iPad
        // All of which are broken by SameSite=None, because they use the iOS networking
        // stack.
        if(userAgent.contains("CPU iPhone OS 12") || userAgent.contains("iPad; CPU OS 12")){
            return false;
        }

        // Cover Mac OS X based browsers that use the Mac OS networking stack.
        // This includes:
        // - Safari on Mac OS X.
        // This does not include:
        // - Chrome on Mac OS X
        // Because they do not use the Mac OS networking stack.
        if (userAgent.contains("Macintosh; Intel Mac OS X 10_14") &&
                userAgent.contains("Version/") && userAgent.contains("Safari")) {
            return false;
        }

        // Cover Chrome 50-69, because some versions are broken by SameSite=None,
        // and none in this range require it.
        // Note: this covers some pre-Chromium Edge versions,
        // but pre-Chromium Edge does not require SameSite=None.
        if(userAgent.contains("Chrome/5") || userAgent.contains("Chrome/6")){
            return false;
        }

        return true;
    } 
    
    */
}
