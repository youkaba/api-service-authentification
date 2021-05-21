/**
 * 
 */
package ca.qc.banq.gia.authentication.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.qc.banq.gia.authentication.exceptions.GIAException;
import ca.qc.banq.gia.authentication.helpers.AuthHelperAAD;
import ca.qc.banq.gia.authentication.helpers.AuthHelperB2C;
import ca.qc.banq.gia.authentication.helpers.HttpClientHelper;
import ca.qc.banq.gia.authentication.models.UserInfo;
import ca.qc.banq.gia.authentication.servicesmetier.GiaBackOfficeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-17
 */
@Slf4j
@RestController
@RequestMapping("/api/fo")
@Api(description = "Services web front-office")
public class GiaFrontOfficeControllerImpl implements GiaFrontOfficeController {

	@Autowired
	AuthHelperB2C authHelperB2C;
	
	@Autowired
	AuthHelperAAD authHelperAAD;
	
	@Value("${server.host}")
	String serverHost;
	
	@Value("${server.servlet.context-path}")
	String servletPath;
	
	@Autowired
	GiaBackOfficeService business;
	
	/*
	 * (non-javadoc)
	 * @see ca.qc.banq.gia.authentication.rest.GiaFrontOfficeController#getUserFromGraph(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
    @GetMapping("/obtenirInfosUtilisateur")
    @ApiOperation("Retourne les infos de l'utilisateur connecte")
	public UserInfo getConnectedUser(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Throwable {
    	String access_token = httpRequest.getHeader("Authorization");
    	if(access_token == null || access_token.isEmpty()) throw new GIAException("invalid.access_token");
    	HttpHeaders requestHeaders = new HttpHeaders();
	  	requestHeaders.setContentType(MediaType.APPLICATION_JSON);
	  	requestHeaders.setBearerAuth(access_token);
	  	UserInfo infos = HttpClientHelper.callRestAPI(authHelperAAD.getMsGraphEndpointHost() + "v1.0/me", HttpMethod.GET, null, UserInfo.class, null, requestHeaders);
	  	return infos;
	}

}
