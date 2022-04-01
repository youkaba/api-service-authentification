// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package ca.qc.banq.gia.authentication.helpers;

import ca.qc.banq.gia.authentication.models.AppPayload;
import ca.qc.banq.gia.authentication.models.StateData;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static ca.qc.banq.gia.authentication.helpers.HttpClientHelper.*;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Helpers for managing session
 *
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-12
 */
public class SessionManagementHelper {

    public static final String STATE = "state";
    private static final String STATES = "states";
    private static final Integer STATE_TTL = 3600;

    public static final String FAILED_TO_VALIDATE_MESSAGE = "Failed to validate data received from Authorization service - ";
    public static final String[] SECURED_PATH = new String[]{"/", "/apps", "/env", "/doc", "/oups", "/apidoc", "/h2-console", "", "", "",};

    public static StateData validateState(HttpSession session, String state) throws Exception {
        if (StringUtils.isNotEmpty(state)) {
            StateData stateDataInSession = removeStateFromSession(session, state);
            if (stateDataInSession != null) {
                return stateDataInSession;
            }
        }
        throw new Exception(FAILED_TO_VALIDATE_MESSAGE + "could not validate state");
    }

    @SuppressWarnings("unchecked")
    private static StateData removeStateFromSession(HttpSession session, String state) {
        Map<String, StateData> states = (Map<String, StateData>) session.getAttribute(STATES);
        if (states != null) {
            eliminateExpiredStates(states);
            StateData stateData = states.get(state);
            if (stateData != null) {
                states.remove(state);
                return stateData;
            }
        }
        return null;
    }

    private static void eliminateExpiredStates(Map<String, StateData> map) {
        Iterator<Map.Entry<String, StateData>> it = map.entrySet().iterator();

        Date currTime = new Date();
        while (it.hasNext()) {
            Map.Entry<String, StateData> entry = it.next();
            long diffInSeconds = TimeUnit.MILLISECONDS.
                    toSeconds(currTime.getTime() - entry.getValue().getExpirationDate().getTime());

            if (diffInSeconds > STATE_TTL) {
                it.remove();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void storeStateAndNonceInSession(HttpSession session, String state, String nonce) {

        // state parameter to validate response from Authorization server and nonce parameter to validate idToken
        if (session.getAttribute(STATES) == null) {
            session.setAttribute(STATES, new HashMap<String, StateData>());
        }
        ((Map<String, StateData>) session.getAttribute(STATES)).put(state, new StateData(nonce, new Date()));
    }

    public static void storeTokenCacheInSession(HttpServletRequest httpServletRequest, String tokenCache) {
        httpServletRequest.getSession().setAttribute(AuthHelperAAD.TOKEN_CACHE_SESSION_ATTRIBUTE, tokenCache);
    }

    public static void setSessionPrincipal(HttpServletRequest httpRequest, IAuthenticationResult result) {
        httpRequest.getSession().setAttribute(AuthHelperAAD.PRINCIPAL_SESSION_NAME, result);
    }

    public static void removePrincipalFromSession(HttpServletRequest httpRequest) {
        httpRequest.getSession().removeAttribute(AuthHelperAAD.PRINCIPAL_SESSION_NAME);
    }

    public static IAuthenticationResult getAuthSessionObject(HttpServletRequest request) {
        Object principalSession = request.getSession().getAttribute(AuthHelperAAD.PRINCIPAL_SESSION_NAME);
        if (principalSession instanceof IAuthenticationResult) {
            return (IAuthenticationResult) principalSession;
        } else {
            throw new IllegalStateException("Session does not contain principal session name");
        }
    }


    public static String buildRedirectAppHomeUrl(IAuthenticationResult auth, String uid, AppPayload app, String giaUrlPath) {

        // Ajout des parametres de requete dans l'url de redirection vers la page d'accueil de l'application
        String query = (StringUtils.contains(app.getHomeUrl(), "?") ? "&" : "?") + // Si l'url de la page d'accueil contient deja des parametres on rajoute juste un &
                ACCESS_TOKEN + "=" + auth.accessToken() + "&" +   // Token d'acces
                EXPDATE_SESSION_NAME + "=" + auth.expiresOnDate().getTime() + "&" + // Date d'expiration de la session
                UID_SESSION_NAME + "=" + uid + "&" + // Identifiant de l'utilisateur connecte
                CLIENTID_PARAM + "=" + app.getClientId() + "&" +   // Identifiant de l'application
                SIGNIN_URL + "=" + URLEncoder.encode(app.getLoginURL(), UTF_8) + "&" +  // Url de connexion
                SIGNOUT_URL + "=" + URLEncoder.encode(app.getLogoutURL(), UTF_8) + "&" +      // Url de signout
                GIA_URLPATH_PARAM + "=" + URLEncoder.encode(giaUrlPath, UTF_8) + "&" +      // Url de base du service GIA
                GIA_CREATEUSER_ENDPOINT_PARAM + "=" + FRONTOFFICE_APIURL + CREATEUSER_ENDPOINT + "&" +      // Endpoint de creation d'un utilisateur
                GIA_RESETPWD_ENDPOINT_PARAM + "=" + URLEncoder.encode(RESETPWD_ENDPOINT, UTF_8)      // Endpoint de reinitialisation de mot de passe
                ;
        // Retourne l'url de redirection de l'application app
        return app.getHomeUrl().concat(query);
    }
}
