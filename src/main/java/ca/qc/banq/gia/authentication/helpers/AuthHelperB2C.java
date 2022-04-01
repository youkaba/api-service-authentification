// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package ca.qc.banq.gia.authentication.helpers;

import ca.qc.banq.gia.authentication.config.B2CConfig;
import ca.qc.banq.gia.authentication.models.AppPayload;
import ca.qc.banq.gia.authentication.models.TokenResponse;
import com.microsoft.aad.msal4j.*;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.openid.connect.sdk.AuthenticationResponse;
import com.nimbusds.openid.connect.sdk.AuthenticationSuccessResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.naming.ServiceUnavailableException;
import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * Helpers for acquiring authorization codes and tokens from B2C
 *
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-12
 */
@Getter
@Component
@RequiredArgsConstructor
public class AuthHelperB2C {

    final String PRINCIPAL_SESSION_NAME = "principal";
    final String TOKEN_CACHE_SESSION_ATTRIBUTE = "token_cache";

    private final B2CConfig configuration;

    AppPayload app;

    public static boolean checkAuthenticationCode(HttpServletRequest httpRequest) {
        Map<String, String[]> httpParameters = httpRequest.getParameterMap();

        boolean isPostRequest = httpRequest.getMethod().equalsIgnoreCase("POST");
        boolean containsErrorData = httpParameters.containsKey("error");
        boolean containIdToken = httpParameters.containsKey("id_token");
        boolean containsCode = httpParameters.containsKey("code");

        return isPostRequest && containsErrorData || containsCode || containIdToken;
    }

    public void init(AppPayload app) {
        this.app = app;
    }

    private ConfidentialClientApplication createClientApplication() throws MalformedURLException {
        return ConfidentialClientApplication.builder(app.getClientId(),
                        ClientCredentialFactory.createFromSecret(app.getCertSecretValue()))
                .b2cAuthority(configuration.getSignUpSignInAuthority(app.getPolicySignUpSignIn()))
                .build();
    }

    public IAuthenticationResult getAuthResultBySilentFlow(HttpServletRequest httpRequest, String scope) throws Throwable {
        IAuthenticationResult result = getAuthSessionObject(httpRequest);

        IAuthenticationResult updatedResult;
        ConfidentialClientApplication app;
        try {
            app = createClientApplication();

            Object tokenCache = httpRequest.getSession().getAttribute("token_cache");
            if (nonNull(tokenCache)) {
                app.tokenCache().deserialize(tokenCache.toString());
            }

            SilentParameters parameters = SilentParameters.builder(
                    Collections.singleton(scope),
                    result.account()).build();

            CompletableFuture<IAuthenticationResult> future = app.acquireTokenSilently(parameters);

            updatedResult = future.get();
        } catch (ExecutionException e) {
            throw e.getCause();
        }

        if (isNull(updatedResult)) {
            throw new ServiceUnavailableException("authentication result was null");
        }

        //update session with latest token cache
        storeTokenCacheInSession(httpRequest, app.tokenCache().serialize());

        return updatedResult;
    }

    public IAuthenticationResult getAuthResultByAuthCode(
            HttpServletRequest httpServletRequest,
            AuthorizationCode authorizationCode,
            String currentUri, Set<String> scopes) throws Throwable {

        IAuthenticationResult result;
        ConfidentialClientApplication app;
        try {
            app = createClientApplication();
            String authCode = authorizationCode.getValue();
            AuthorizationCodeParameters parameters = AuthorizationCodeParameters
                    .builder(authCode, new URI(currentUri)).scopes(scopes).build();
            Future<IAuthenticationResult> future = app.acquireToken(parameters);
            result = future.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
            throw e.getCause();
        }

        if (isNull(result)) {
            throw new ServiceUnavailableException("authentication result was null");
        }
        storeTokenCacheInSession(httpServletRequest, app.tokenCache().serialize());

        return result;
    }

    public void setSessionPrincipal(HttpServletRequest httpRequest, IAuthenticationResult result) {
        httpRequest.getSession().setAttribute(PRINCIPAL_SESSION_NAME, result);
    }

    public void removePrincipalFromSession(HttpServletRequest httpRequest) {
        httpRequest.getSession().removeAttribute(PRINCIPAL_SESSION_NAME);
    }

    public void updateAuthDataUsingSilentFlow(HttpServletRequest httpRequest) throws Throwable {
        IAuthenticationResult authResult = getAuthResultBySilentFlow(httpRequest, "https://graph.microsoft.com/.default");
        setSessionPrincipal(httpRequest, authResult);
    }

    private void storeTokenCacheInSession(HttpServletRequest httpServletRequest, String tokenCache) {
        httpServletRequest.getSession().setAttribute(TOKEN_CACHE_SESSION_ATTRIBUTE, tokenCache);
    }

    public boolean isAuthenticationSuccessful(AuthenticationResponse authResponse) {
        return authResponse instanceof AuthenticationSuccessResponse;
    }

    public boolean isAuthenticated(HttpServletRequest request) {
        return request.getSession().getAttribute(PRINCIPAL_SESSION_NAME) != null;
    }

    public IAuthenticationResult getAuthSessionObject(HttpServletRequest request) {
        Object principalSession = request.getSession().getAttribute(PRINCIPAL_SESSION_NAME);
        if (principalSession instanceof IAuthenticationResult) {
            return (IAuthenticationResult) principalSession;
        } else {
            throw new IllegalStateException();
        }
    }

    public boolean containsAuthenticationCode(HttpServletRequest httpRequest) {
        return checkAuthenticationCode(httpRequest);
    }

    /***
     * Obtien un token d'acces a partir d'une authorization
     */
    public TokenResponse getToken(HttpServletRequest request) throws Exception {
        String code = request.getParameter("code");
        String url = configuration.getSignUpSignInAuthority(app.getPolicySignUpSignIn()).replace("/tfp", "") + "oauth2/v2.0/token?" +
                "grant_type=authorization_code&" +
                "code=" + code + "&" +
                "redirect_uri=" + URLEncoder.encode(app.getRedirectApp(), UTF_8) +
                "&client_id=" + app.getClientId() +
                "&client_secret=" + app.getCertSecretValue() +
                "&scope=" + URLEncoder.encode("openid offline_access profile " + app.getApiScope(), UTF_8) +
                (StringUtils.isEmpty(request.getParameter("claims")) ? "" : "&claims=" + request.getParameter("claims"));

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED); //MediaType.APPLICATION_JSON);

        return HttpClientHelper.callRestAPI(url, HttpMethod.POST, null, TokenResponse.class, null, requestHeaders);
    }

    /**
     * Renouvelle un token d'acces (apres son expiration)
     */
    public TokenResponse refreshToken(String refresh_token, HttpServletRequest request) {
        String url = configuration.getSignUpSignInAuthority(app.getPolicySignUpSignIn()).replace("/tfp", "") + "oauth2/v2.0/token?" +
                "grant_type=refresh_token&" +
                "refresh_token=" + refresh_token + "&" +
                "redirect_uri=" + URLEncoder.encode(app.getRedirectApp(), UTF_8) +
                "&client_id=" + app.getClientId() +
                "&client_secret=" + app.getCertSecretValue() +
                "&scope=" + URLEncoder.encode("openid offline_access profile " + app.getApiScope(), UTF_8) +
                (StringUtils.isEmpty(request.getParameter("claims")) ? "" : "&claims=" + request.getParameter("claims"));

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED); //MediaType.APPLICATION_JSON);

        return HttpClientHelper.callRestAPI(url, HttpMethod.POST, null, TokenResponse.class, null, requestHeaders);
    }
}
