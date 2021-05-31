// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package ca.qc.banq.gia.authentication.rest;

import java.net.URLEncoder;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
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
import ca.qc.banq.gia.authentication.models.TokenResponse;
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
	 */
	@RequestMapping("/home")
    public ModelAndView home() throws Throwable {
		ModelAndView mav = new ModelAndView("index");
		List<AppPayload> apps = appService.findAll();
		mav.addObject("apps", apps);
		return mav;
	}
	
	/**
	 * Home Page Authentification B2C
	 * @param httpRequest
	 * @return
	 * @throws ParseException
	 */
    @RequestMapping("/redirect2_b2c")
    public void securePageB2C(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Throwable {
    	
    	IAuthenticationResult auth = authHelperB2C.getAuthSessionObject(httpRequest);
    	Map<String, Object> claims = JWTParser.parse(auth.idToken()).getJWTClaimsSet().getClaims();
    	log.error("claims=" + claims.toString());
    	String clientId = claims.get("aud").toString();
    	String uid = claims.get(HttpClientHelper.BAnQ_CUSTOM_USERID).toString(); //String uid = "11340729";
    	String code = httpRequest.getParameter("code");
    	log.error("auth.accessToken = " + auth.accessToken());
    	clientId = StringUtils.removeStart(clientId, "[");
    	clientId = StringUtils.removeEnd(clientId, "]");
    	AppPayload app = appService.findByClientId(clientId);
    	
        if( app != null) {
        	
        	String query = "?" + HttpClientHelper.ACCESS_TOKEN + "=" + auth.accessToken() + "&" + HttpClientHelper.EXPDATE_SESSION_NAME + "=" + String.valueOf(auth.expiresOnDate().getTime()) + "&" + HttpClientHelper.UID_SESSION_NAME + "=" + uid + "&" + AuthFilter.APP_ID + "=" + clientId + "&" + HttpClientHelper.SIGNIN_URL + "=" +  URLEncoder.encode(app.getLoginURL(), "UTF-8") + "&" + HttpClientHelper.SIGNOUT_URL + "=" +  URLEncoder.encode(app.getLogoutURL(), "UTF-8") ;
        	httpResponse.sendRedirect(app.getHomeUrl().concat(query));
        	
        } else {
        	httpResponse.setStatus(500);
			httpRequest.setAttribute("error", "unable to find IAuthenticationResult");
			httpRequest.getRequestDispatcher("/error").forward(httpRequest, httpResponse);
        }
    }
    
    /***
     * Obtien un token d'acces a partir d'une authorization
     * @param code
     * @param app
     * @param request
     * @return
     * @throws Exception
     */
	public TokenResponse getToken(String code, AppPayload app, HttpServletRequest request) throws Exception {
        String url = authHelperB2C.getConfiguration().getSignUpSignInAuthority(app.getPolicySignUpSignIn()).replace("/tfp", "") + "oauth2/v2.0/token?" +
                "grant_type=authorization_code&" +
                "code="+ code +"&" +
                "redirect_uri=" + URLEncoder.encode(app.getRedirectApp(), "UTF-8") +
                "&client_id=" + app.getClientId() +
                "&client_secret=" + app.getCertSecretValue() +
                "&scope=" + URLEncoder.encode("openid offline_access profile " + app.getApiScope(), "UTF-8") +
                (StringUtils.isEmpty(request.getParameter("claims")) ? "" : "&claims=" + request.getParameter("claims"));
        
    	HttpHeaders requestHeaders = new HttpHeaders();
	  	requestHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED); //MediaType.APPLICATION_JSON);
	  	
	  	TokenResponse token = HttpClientHelper.callRestAPI(url, HttpMethod.POST, null, TokenResponse.class, null, requestHeaders);
	  	return token;
	}

	/**
	 * Renouvelle un token d'acces (apres son expiration)
	 * @param refresh_token
	 * @param app
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public TokenResponse refreshToken(String refresh_token, AppPayload app, HttpServletRequest request) throws Exception {
        String url = authHelperB2C.getConfiguration().getSignUpSignInAuthority(app.getPolicySignUpSignIn()).replace("/tfp", "") + "oauth2/v2.0/token?" +
                "grant_type=refresh_token&" +
                "refresh_token="+ refresh_token +"&" +
                "redirect_uri=" + URLEncoder.encode(app.getRedirectApp(), "UTF-8") +
                "&client_id=" + app.getClientId() +
                "&client_secret=" + app.getCertSecretValue() +
                "&scope=" + URLEncoder.encode("openid offline_access profile " + app.getApiScope(), "UTF-8") +
                (StringUtils.isEmpty(request.getParameter("claims")) ? "" : "&claims=" + request.getParameter("claims"));
        
    	HttpHeaders requestHeaders = new HttpHeaders();
	  	requestHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED); //MediaType.APPLICATION_JSON);
	  	
	  	TokenResponse token = HttpClientHelper.callRestAPI(url, HttpMethod.POST, null, TokenResponse.class, null, requestHeaders);
	  	return token;
	}
	
	/**
	 * Recupere les infos de l'utilisateur connecte a partir de l'API Microsoft Graph
	 * @param token
	 * @return
	 */
	public UserInfo getUserInfos(String token) {
    	HttpHeaders requestHeaders = new HttpHeaders();
	  	requestHeaders.setContentType(MediaType.APPLICATION_JSON);
	  	requestHeaders.setBearerAuth(token);
    	UserInfo infos = HttpClientHelper.callRestAPI(authHelperAAD.getMsGraphEndpointHost() + "v1.0/me", HttpMethod.GET, null, UserInfo.class, null, requestHeaders);
    	return infos;
	}
    
    /**
     * Authentification Azure AD (pour les utilisateurs employes)
     * @param httpRequest requete http
     * @param httpResponse reponse http
     * @throws Throwable
     */
    @RequestMapping("/redirect2_aad2")
    public void securePageAAD(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Throwable {
    	IAuthenticationResult auth = authHelperAAD.getAuthResultBySilentFlow(httpRequest, httpResponse);
    	Map<String, Object> claims = JWTParser.parse(auth.idToken()).getJWTClaimsSet().getClaims();
    	String clientId = claims.get("aud").toString();
    	clientId = StringUtils.removeStart(clientId, "[");
    	clientId = StringUtils.removeEnd(clientId, "]");
    	System.err.println("claims=" + claims.toString());
    	System.err.println("auth.accessToken = " + auth.accessToken());
    	//String uid = claims.get(HttpClientHelper.UID_SESSION_NAME).toString();
    	AppPayload app = appService.findByClientId(clientId);
    	
    	String uid = "Stephane.Tellier";
    	
        if(auth != null && app != null) {
        	UserInfo user = getUserInfos(auth.accessToken());
        	uid = user.getUserPrincipalName();
        	String query = "?" + HttpClientHelper.ACCESS_TOKEN + "=" + auth.accessToken() + "&" + HttpClientHelper.EXPDATE_SESSION_NAME + "=" + String.valueOf(auth.expiresOnDate().getTime()) + "&" + HttpClientHelper.UID_SESSION_NAME + "=" + uid + "&" + AuthFilter.APP_ID + "=" + clientId + "&" + HttpClientHelper.SIGNIN_URL + "=" +  URLEncoder.encode(app.getLoginURL(), "UTF-8") + "&" + HttpClientHelper.SIGNOUT_URL + "=" +  URLEncoder.encode(app.getLogoutURL(), "UTF-8") ;
	        httpResponse.sendRedirect(app.getHomeUrl().concat(query));
        } else {
        	httpResponse.setStatus(500);
			httpRequest.setAttribute("error", "unable to find IAuthenticationResult or clientId");
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
