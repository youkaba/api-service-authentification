// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package ca.qc.banq.gia.authentication.rest;

import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import ca.qc.banq.gia.authentication.entities.TypeAuth;
import ca.qc.banq.gia.authentication.exceptions.GIAException;
import ca.qc.banq.gia.authentication.filter.AuthFilterAAD;
import ca.qc.banq.gia.authentication.filter.AuthFilterB2C;
import ca.qc.banq.gia.authentication.helpers.AuthHelperAAD;
import ca.qc.banq.gia.authentication.helpers.AuthHelperB2C;
import ca.qc.banq.gia.authentication.helpers.HttpClientHelper;
import ca.qc.banq.gia.authentication.models.AppPayload;
import ca.qc.banq.gia.authentication.servicesmetier.GiaBackOfficeService;
import lombok.extern.slf4j.Slf4j;


/**
 * Controller exposing application endpoints
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-12
 */
@Slf4j
@Controller
public class AuthPageController {

	@Autowired
	AuthHelperB2C authHelperB2C;
	
	@Autowired
	AuthHelperAAD authHelperAAD;
	
	@Value("${server.host}")
	String serverHost;
	
	@Value("${server.servlet.context-path}")
	String servletPath;

	@Autowired
	GiaBackOfficeService appService;

	@Autowired
	AuthFilterB2C filterB2C;
	
	@Autowired
	AuthFilterAAD filterAAD;
	
	/**
	 * Redirection vers une authentification Azure B2C
	 * @param httpRequest
	 * @param httpResponse
	 * @throws Throwable
	 */
	@RequestMapping(HttpClientHelper.REDIRECTB2C_ENDPOINT)
	public void redirectB2C(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Throwable {
		filterB2C.doFilter(httpRequest, httpResponse);
	}

	/**
	 * Redirection vers une authentification Azure AD
	 * @param httpRequest
	 * @param httpResponse
	 * @throws Throwable
	 */
	@RequestMapping(HttpClientHelper.REDIRECTAAD_ENDPOINT)
	public void redirectAAD(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Throwable {
		filterAAD.doFilter(httpRequest, httpResponse);
	}
	
	/**
	 * Connexion a une application
	 * @param httpRequest
	 * @param httpResponse
	 * @throws Throwable
	 */
	@RequestMapping(HttpClientHelper.SIGNIN_ENDPOINT)
    public void signIn(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Throwable {
    	String clientId = httpRequest.getParameter(HttpClientHelper.CLIENTID_PARAM);
    	AppPayload app = clientId != null ? appService.findByClientId(clientId) : null;
    	if(app == null) throw new GIAException("unable to find appid") ;
    	if(app.getTypeAuth().equals(TypeAuth.B2C)) {
    		filterB2C.getAuthHelper().init(app);
    		filterB2C.doFilter(httpRequest, httpResponse);
    	} else {
    		filterAAD.getAuthHelper().init(app);
    		filterAAD.doFilter(httpRequest, httpResponse);
    	}
    }
	
    /**
     * Deconnexion d'une application
     * @param httpRequest
     * @param httpResponse
     * @throws Throwable
     */
    @RequestMapping(HttpClientHelper.SIGNOUT_ENDPOINT)
    public void signOut(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Throwable {
    	String clientId = httpRequest.getParameter(HttpClientHelper.CLIENTID_PARAM);
    	AppPayload app = clientId != null ? appService.findByClientId(clientId) : null;
    	if(app == null) throw new GIAException("unable to find appid") ;
        httpRequest.getSession().invalidate();
        if(app.getTypeAuth().equals(TypeAuth.B2C)) httpResponse.sendRedirect(app.getLoginURL() );
        else httpResponse.sendRedirect("https://login.microsoftonline.com/common/oauth2/v2.0/logout?post_logout_redirect_uri=" + URLEncoder.encode(app.getRedirectApp(), "UTF-8") );
    }
    
    /**
     * Ouvre le flux de re-initialisation du mot de passe de l'usager qui cliquera sur ce lien
     * @param httpRequest
     * @param httpResponse
     * @throws Throwable
     */
    @RequestMapping(HttpClientHelper.RESETPWD_ENDPOINT)
	public void resetUserPassword(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Throwable {
    	String clientId = httpRequest.getParameter(HttpClientHelper.CLIENTID_PARAM);
    	AppPayload app = clientId != null ? appService.findByClientId(clientId) : null;
    	if(app == null) throw new GIAException("unable to find appid") ;
    	String url = authHelperB2C.getConfiguration().getAuthorityBase() + 
    			"oauth2/v2.0/authorize?p=" + app.getPolicyResetPassword() + "&" + 
    			"client_id=" + app.getClientId() + "&" + 
    			"nonce=defaultNonce" + "&" + 
    			"redirect_uri=" + URLEncoder.encode(app.getLoginURL(), "UTF-8") + "&" +
    			"scope=openid&response_type=id_token&prompt=login"
    			;
    	httpResponse.sendRedirect(url);
	}

    
}
