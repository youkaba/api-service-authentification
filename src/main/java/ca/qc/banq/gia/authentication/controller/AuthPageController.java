// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package ca.qc.banq.gia.authentication.controller;

import ca.qc.banq.gia.authentication.entities.TypeAuth;
import ca.qc.banq.gia.authentication.exceptions.GIAException;
import ca.qc.banq.gia.authentication.filter.AuthFilterAAD;
import ca.qc.banq.gia.authentication.filter.AuthFilterB2C;
import ca.qc.banq.gia.authentication.helpers.AuthHelperB2C;
import ca.qc.banq.gia.authentication.helpers.HttpClientHelper;
import ca.qc.banq.gia.authentication.models.AppPayload;
import ca.qc.banq.gia.authentication.servicesmetier.GiaBackOfficeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


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

    @Value("${server.host}")
    String serverHost;

    @Value("${server.servlet.context-path}")
    String servletPath;

    private final AuthHelperB2C authHelperB2C;
    private final GiaBackOfficeService appService;
    private final AuthFilterB2C filterB2C;
    private final AuthFilterAAD filterAAD;


    /**
     * Redirection vers une authentification Azure B2C
     */
    @RequestMapping(HttpClientHelper.REDIRECTB2C_ENDPOINT)
    public void redirectB2C(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Throwable {
        filterB2C.doFilter(httpRequest, httpResponse);
    }

    /**
     * Redirection vers une authentification Azure AD
     */
    @RequestMapping(HttpClientHelper.REDIRECTAAD_ENDPOINT)
    public void redirectAAD(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Throwable {
        filterAAD.doFilter(httpRequest, httpResponse);
    }

    /**
     * Connexion a une application
     */
    @RequestMapping(HttpClientHelper.SIGNIN_ENDPOINT)
    public void signIn(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Throwable {
        String clientId = httpRequest.getParameter(HttpClientHelper.CLIENTID_PARAM);
        AppPayload app = clientId != null ? appService.findByClientId(clientId) : null;
        if (app == null) throw new GIAException("unable to find appid");
        if (app.getTypeAuth().equals(TypeAuth.B2C)) {
            filterB2C.getAuthHelper().init(app);
            filterB2C.doFilter(httpRequest, httpResponse);
        } else {
            filterAAD.getAuthHelper().init(app);
            filterAAD.doFilter(httpRequest, httpResponse);
        }
    }

    /**
     * Deconnexion d'une application
     */
    @RequestMapping(HttpClientHelper.SIGNOUT_ENDPOINT)
    public void signOut(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Throwable {
        String clientId = httpRequest.getParameter(HttpClientHelper.CLIENTID_PARAM);
        AppPayload app = clientId != null ? appService.findByClientId(clientId) : null;
        if (app == null) throw new GIAException("unable to find appid");
        httpRequest.getSession().invalidate();
        if (app.getTypeAuth().equals(TypeAuth.B2C)) httpResponse.sendRedirect(app.getLoginURL());
        else
            httpResponse.sendRedirect("https://login.microsoftonline.com/common/oauth2/v2.0/logout?post_logout_redirect_uri=" + URLEncoder.encode(app.getRedirectApp(), StandardCharsets.UTF_8));
    }

    /**
     * Ouvre le flux de re-initialisation du mot de passe de l'usager qui cliquera sur ce lien
     */
    @RequestMapping(HttpClientHelper.RESETPWD_ENDPOINT)
    public void resetUserPassword(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Throwable {

        // Recuperation de l'id de l'application dans la requete
        String clientId = httpRequest.getParameter(HttpClientHelper.CLIENTID_PARAM);
        AppPayload app = clientId != null ? appService.findByClientId(clientId) : null;
        if (app == null) throw new GIAException("unable to find appid");

        // Initialisation de l'application dans le helper
        if (app.getTypeAuth().equals(TypeAuth.B2C)) filterB2C.getAuthHelper().init(app);
        else filterAAD.getAuthHelper().init(app);

        // Construction du lien de redirection vers le flux de reinitialisation Azure AD de l'application
        String url = authHelperB2C.getConfiguration().getAuthorityBase().replace("/tfp", "") +
                "oauth2/v2.0/authorize?p=" + app.getPolicyResetPassword() + "&" +
                "client_id=" + app.getClientId() + "&" +
                "nonce=defaultNonce" + "&" +
                "redirect_uri=" + URLEncoder.encode(app.getRedirectApp(), StandardCharsets.UTF_8) + "&" +
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
        httpResponse.sendRedirect("https://login.microsoftonline.com/common/oauth2/v2.0/logout?post_logout_redirect_uri=" + URLEncoder.encode(serverHost.concat(servletPath), StandardCharsets.UTF_8));
    }
}
