// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package ca.qc.banq.gia.authentication.rest;

import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.nimbusds.jwt.JWTParser;

import ca.qc.banq.gia.authentication.entities.TypeAuth;
import ca.qc.banq.gia.authentication.filter.AuthFilter;
import ca.qc.banq.gia.authentication.helpers.AuthHelperAAD;
import ca.qc.banq.gia.authentication.helpers.AuthHelperB2C;
import ca.qc.banq.gia.authentication.helpers.HttpClientHelper;
import ca.qc.banq.gia.authentication.models.AppPayload;
import ca.qc.banq.gia.authentication.models.UserInfo;
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

	/**
	 * Page d'Accueil de l'idp
	 * @return
	 * @throws Throwable
	 *
	@RequestMapping("/home")
    public ModelAndView home() throws Throwable {
		ModelAndView mav = new ModelAndView("index2");
		List<AppPayload> apps = appService.findAll();
		mav.addObject("apps", apps);
		return mav;
	} */
	
	@RequestMapping("/redirect2_aad2")
	public void redirectAAD(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Throwable {

    	IAuthenticationResult auth = authHelperAAD.getAuthResultBySilentFlow(httpRequest, httpResponse);
    	
        if(auth != null) {
        	Map<String, Object> claims = JWTParser.parse(auth.idToken()).getJWTClaimsSet().getClaims();
        	log.error("claims = " + claims);
        	UserInfo user = authHelperAAD.getUserInfos(auth.accessToken());
        	String uid = user.getUserPrincipalName();
        	String query = "?" + HttpClientHelper.ACCESS_TOKEN + "=" + auth.accessToken() + "&" + HttpClientHelper.EXPDATE_SESSION_NAME + "=" + String.valueOf(auth.expiresOnDate().getTime()) + "&" + HttpClientHelper.UID_SESSION_NAME + "=" + uid + "&" + AuthFilter.APP_ID + "=" + authHelperAAD.getApp().getClientId() + "&" + HttpClientHelper.SIGNIN_URL + "=" +  URLEncoder.encode(authHelperAAD.getApp().getLoginURL(), "UTF-8") + "&" + HttpClientHelper.SIGNOUT_URL + "=" +  URLEncoder.encode(authHelperAAD.getApp().getLogoutURL(), "UTF-8") ;
	        httpResponse.sendRedirect(authHelperAAD.getApp().getHomeUrl().concat(query));
        } else {
        	httpResponse.setStatus(500);
			httpRequest.setAttribute("error", "unable to find IAuthenticationResult");
			httpRequest.getRequestDispatcher("/error").forward(httpRequest, httpResponse);
        }
	}
	
    /**
     * Deconnexion d'une application
     * @param httpRequest
     * @param httpResponse
     * @throws Throwable
     */
    @RequestMapping("/sign_out")
    public void signOut(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Throwable {
    	String clientId = httpRequest.getParameter(AuthFilter.APP_ID);
    	AppPayload app = clientId != null ? appService.findByClientId(clientId) : null;
    	if(app != null) {
	        httpRequest.getSession().invalidate();
	        if(app.getTypeAuth().equals(TypeAuth.B2C)) httpResponse.sendRedirect(app.getLoginURL() );
	        else httpResponse.sendRedirect("https://login.microsoftonline.com/common/oauth2/v2.0/logout?post_logout_redirect_uri=" + URLEncoder.encode(app.getRedirectApp(), "UTF-8") );
    	} else {
        	httpResponse.setStatus(500);
			httpRequest.setAttribute("error", "unable to find appid");
			httpRequest.getRequestDispatcher("/error").forward(httpRequest, httpResponse);
        }
    }
    
}
