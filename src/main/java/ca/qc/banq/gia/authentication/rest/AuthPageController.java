// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package ca.qc.banq.gia.authentication.rest;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.MsalInteractionRequiredException;
import com.nimbusds.jwt.JWTParser;

import ca.qc.banq.gia.authentication.entities.TypeAuth;
import ca.qc.banq.gia.authentication.helpers.AuthHelperAAD;
import ca.qc.banq.gia.authentication.helpers.AuthHelperB2C;
import ca.qc.banq.gia.authentication.helpers.HttpClientHelper;
import ca.qc.banq.gia.authentication.helpers.SessionManagementHelper;
import ca.qc.banq.gia.authentication.models.AppPayload;
import ca.qc.banq.gia.authentication.servicesmetier.GiaBackOfficeService;


/**
 * Controller exposing application endpoints
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-12
 */
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
	 * Home Page Authentification B2C
	 * @param httpRequest
	 * @return
	 * @throws ParseException
	 */
    @RequestMapping("/redirect_app")
    public ModelAndView securePageB2C(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Throwable {
    	ModelAndView mav = new ModelAndView("auth_page_b2c");
        setAccountInfoB2C(mav, httpRequest);
        return mav;
    	/*
    	 * francis.djiomou@banqb2cdev.onmicrosoft.com
    	IAuthenticationResult auth = authHelperB2C.getAuthSessionObject(httpRequest);
    	Map<String, Object> claims = JWTParser.parse(auth.idToken()).getJWTClaimsSet().getClaims();
    	String clientId = claims.get("aud").toString();
    	AppPayload app = appService.findByClientId(clientId);
    	
        if(auth != null && app != null) {
	        httpResponse.addHeader(HttpClientHelper.ACCESS_TOKEN, auth.accessToken());
	        httpResponse.addHeader(HttpClientHelper.EXPDATE_SESSION_NAME, String.valueOf(auth.expiresOnDate().getTime()) );
	        httpResponse.addHeader(HttpClientHelper.IDTOKEN_SESSION_NAME, auth.idToken());
	        httpResponse.sendRedirect(app.getHomeUrl());
        } else {
        	httpResponse.setStatus(500);
			httpRequest.setAttribute("error", "unable to find IAuthenticationResult");
			httpRequest.getRequestDispatcher("/error").forward(httpRequest, httpResponse);
        }
        */
    }

    /**
     * Home Page Authentification AAD
     * @param httpRequest
     * @return
     * @throws ParseException
     */
    @RequestMapping("/aad")
    public ModelAndView securePageAAD(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ParseException {
        ModelAndView mav = new ModelAndView("auth_page_aad");
        setAccountInfoAAD(mav, httpRequest);
        return mav;
    }

    /**
     * Deconnexion B2C
     * @param httpRequest
     * @param response
     * @throws IOException
     */
    @RequestMapping("/sign_out_b2c")
    public void signOutB2C(HttpServletRequest httpRequest, HttpServletResponse response) throws IOException {
        httpRequest.getSession().invalidate();
        String redirectUrl = serverHost.concat(servletPath);
        response.sendRedirect(redirectUrl + "?appid=" + authHelperB2C.getApp().getId() );
    }
    
    /**
     * Deconnexion AAD
     * @param httpRequest
     * @param response
     * @throws IOException
     */
    @RequestMapping("/sign_out_aad")
    public void signOutAAD(HttpServletRequest httpRequest, HttpServletResponse response) throws IOException {
        httpRequest.getSession().invalidate();
        String endSessionEndpoint = "https://login.microsoftonline.com/common/oauth2/v2.0/logout";
        String redirectUrl = serverHost.concat(servletPath);
        response.sendRedirect(endSessionEndpoint + "?post_logout_redirect_uri=" + URLEncoder.encode(redirectUrl, "UTF-8") + "&appid=" + authHelperAAD.getApp().getId() );
    }
    
    /**
     * Recupere les infos de l'usager connecte
     * @param httpRequest
     * @param httpResponse
     * @return
     * @throws Throwable
     */
    @RequestMapping("/graph/me")
    public ModelAndView getUserFromGraph(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Throwable {

        IAuthenticationResult result;
        ModelAndView mav;
        try {
            result = authHelperAAD.getAuthResultBySilentFlow(httpRequest, httpResponse);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof MsalInteractionRequiredException) {

                // If silent call returns MsalInteractionRequired, then redirect to Authorization endpoint
                // so user can consent to new scopes
                String state = UUID.randomUUID().toString();
                String nonce = UUID.randomUUID().toString();

                SessionManagementHelper.storeStateAndNonceInSession(httpRequest.getSession(), state, nonce);
                String authorizationCodeUrl = authHelperAAD.getAuthorizationCodeUrl(
                        httpRequest.getParameter("claims"),
                        "User.Read",
                        authHelperAAD.getRedirectUriGraph(),
                        state,
                        nonce);

                return new ModelAndView("redirect:" + authorizationCodeUrl);
            } else {

                mav = new ModelAndView("error");
                mav.addObject("error", e);
                return mav;
            }
        }

        if (result == null) {
            mav = new ModelAndView("error");
            mav.addObject("error", new Exception("AuthenticationResult not found in session."));
        } else {
            mav = new ModelAndView("auth_page_aad");
            setAccountInfoAAD(mav, httpRequest);

            try {
                mav.addObject("userInfo", getUserInfoFromGraph(result.accessToken()));

                return mav;
            } catch (Exception e) {
                mav = new ModelAndView("error");
                mav.addObject("error", e);
            }
        }
        return mav;
    }

    /**
     * Recupere les infos de l'utilisateur connecte a partir de l'API Microsoft Graph
     * @param accessToken
     * @return
     * @throws Exception
     */
    private String getUserInfoFromGraph(String accessToken) throws Exception {
        // Microsoft Graph user endpoint
        URL url = new URL(authHelperAAD.getMsGraphEndpointHost() + "v1.0/me");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // Set the appropriate header fields in the request header.
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Accept", "application/json");

        String response = HttpClientHelper.getResponseStringFromConn(conn);

        int responseCode = conn.getResponseCode();
        if(responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException(response);
        }

        JSONObject responseObject = HttpClientHelper.processResponse(responseCode, response);
        return responseObject.toString();
    }
    
    /**
     * Met a jour les infos de l'utilisateur sur la page Homme de B2C
     * @param model
     * @param httpRequest
     * @throws ParseException
     */
    @SuppressWarnings("static-access")
	private void setAccountInfoB2C(ModelAndView model, HttpServletRequest httpRequest) throws ParseException {
        IAuthenticationResult auth = authHelperB2C.getAuthSessionObject(httpRequest);
        model.addObject("idTokenClaims", JWTParser.parse(auth.idToken()).getJWTClaimsSet().getClaims());
        model.addObject("account", auth.account());
        model.addObject("app", authHelperB2C.getApp());
    }

    /**
     * Met a jour les infos de l'utilisateur sur la page Homme de AAD
     * @param model
     * @param httpRequest
     * @throws ParseException
     */
    private void setAccountInfoAAD(ModelAndView model, HttpServletRequest httpRequest) throws ParseException {
        IAuthenticationResult auth = SessionManagementHelper.getAuthSessionObject(httpRequest);
        String tenantId = JWTParser.parse(auth.idToken()).getJWTClaimsSet().getStringClaim("tid");
        model.addObject("tenantId", tenantId);
        model.addObject("account", SessionManagementHelper.getAuthSessionObject(httpRequest).account());
        model.addObject("app", authHelperAAD.getApp());
    }
}
