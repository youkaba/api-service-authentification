// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package ca.qc.banq.gia.authentication.controller;

import ca.qc.banq.gia.authentication.filter.AuthFilterAAD;
import ca.qc.banq.gia.authentication.filter.AuthFilterB2C;
import ca.qc.banq.gia.authentication.helpers.AuthHelperB2C;
import ca.qc.banq.gia.authentication.models.AppPayload;
import ca.qc.banq.gia.authentication.services.GiaBackOfficeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;

import static ca.qc.banq.gia.authentication.entities.AuthenticationType.B2C;
import static ca.qc.banq.gia.authentication.helpers.HttpClientHelper.*;
import static java.nio.charset.StandardCharsets.UTF_8;


/**
 * Controller exposing application endpoints
 * Exposition des services web de type Request pour les application interfacant avec GIA
 *
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-12
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthPageController {

    private final GiaBackOfficeService giaBackOfficeService;
    private final AuthFilterB2C authFilterB2C;

    private final AuthHelperB2C authHelperB2C;
    private final AuthFilterAAD authFilterAAD;
    @Value("${server.host}")
    private String serverHost;
    @Value("${server.servlet.context-path}")
    private String servletPath;

    /**
     * Redirection vers une authentification Azure B2C
     */
    @RequestMapping(REDIRECTB2C_ENDPOINT)
    public void redirectB2C(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Throwable {
        authFilterB2C.doFilter(httpRequest, httpResponse);
    }

    /**
     * Redirection vers une authentification Azure AD
     */
    @RequestMapping(REDIRECTAAD_ENDPOINT)
    public void redirectAAD(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Throwable {
        authFilterAAD.doFilter(httpRequest, httpResponse);
    }

    /**
     * Connexion a une application
     */
    @RequestMapping(SIGNIN_ENDPOINT)
    public void signIn(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Throwable {
        AppPayload app = giaBackOfficeService.checkClientID(httpRequest);
        if (app.getAuthenticationType().equals(B2C)) {
            authFilterB2C.getAuthHelper().init(app);
            authFilterB2C.doFilter(httpRequest, httpResponse);
        } else {
            authFilterAAD.getAuthHelper().init(app);
            authFilterAAD.doFilter(httpRequest, httpResponse);
        }
    }

    /**
     * Deconnexion d'une application
     */
    @RequestMapping(SIGNOUT_ENDPOINT)
    public void signOut(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Throwable {
        AppPayload app = giaBackOfficeService.checkClientID(httpRequest);

        httpRequest.getSession().invalidate();
        if (app.getAuthenticationType().equals(B2C)) {
            httpResponse.sendRedirect(app.getLoginURL());
        } else {
            httpResponse.sendRedirect("https://login.microsoftonline.com/common/oauth2/v2.0/logout?post_logout_redirect_uri=" + URLEncoder.encode(app.getRedirectApp(), UTF_8));
        }
    }

    /**
     * Ouvre le flux de re-initialisation du mot de passe de l'usager qui cliquera sur ce lien
     */
    @RequestMapping(RESETPWD_ENDPOINT)
    public void resetUserPassword(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Throwable {
        AppPayload app = giaBackOfficeService.checkClientID(httpRequest);

        // Initialisation de l'application dans le helper
        if (app.getAuthenticationType().equals(B2C)) authFilterB2C.getAuthHelper().init(app);
        else authFilterAAD.getAuthHelper().init(app);

        // Construction du lien de redirection vers le flux de reinitialisation Azure AD de l'application
        String url = authHelperB2C.getAzureB2CConfig().getAuthorityBase().replace("/tfp", "") +
                "oauth2/v2.0/authorize?p=" + app.getPolicyResetPassword() + "&" +
                "client_id=" + app.getClientId() + "&" +
                "nonce=defaultNonce" + "&" +
                "redirect_uri=" + URLEncoder.encode(app.getRedirectApp(), UTF_8) + "&" +
                "scope=openid&response_type=id_token&prompt=login";
        log.info("reset-password url = " + url);

        // Ouverture du flux de reinitialisation du mot de passe
        httpResponse.sendRedirect(url);
    }

    /**
     * Deconnexion du service d'authentification
     */
    @RequestMapping("/signout")
    public void logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Throwable {
        httpRequest.getSession().invalidate();
        httpResponse.sendRedirect("https://login.microsoftonline.com/common/oauth2/v2.0/logout?post_logout_redirect_uri="
                + URLEncoder.encode(serverHost.concat(servletPath), UTF_8));
    }
}
