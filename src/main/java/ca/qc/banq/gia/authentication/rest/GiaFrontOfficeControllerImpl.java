/**
 * 
 */
package ca.qc.banq.gia.authentication.rest;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.qc.banq.gia.authentication.exceptions.GIAException;
import ca.qc.banq.gia.authentication.helpers.AuthHelperAAD;
import ca.qc.banq.gia.authentication.helpers.AuthHelperB2C;
import ca.qc.banq.gia.authentication.helpers.HttpClientHelper;
import ca.qc.banq.gia.authentication.models.AppPayload;
import ca.qc.banq.gia.authentication.models.CreateUserRequestPayload;
import ca.qc.banq.gia.authentication.models.GetTokenRequestPayload;
import ca.qc.banq.gia.authentication.models.TokenResponse;
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
	
	@Override
    @PostMapping(HttpClientHelper.CREATEUSER_ENDPOINT)
    @ApiOperation("Cree un nouvel utilisateur dans Azure B2C")
	public UserInfo createUserIntoAzureB2C(HttpServletRequest httpRequest, CreateUserRequestPayload request) throws Throwable {
		
		// Recuperation des parametres d'entete de la requete
		String appId = httpRequest.getParameter(HttpClientHelper.CLIENTID_PARAM) ;
		
		// Check params
		if(appId == null) throw new GIAException("invalid client_id");
		
		AppPayload app = business.findByClientId(appId);
		if(app == null) throw new GIAException("invalid client_id");
		
		// Initialisation du headers
    	HttpHeaders requestHeaders = new HttpHeaders();
	  	requestHeaders.setContentType(MediaType.APPLICATION_JSON); // MediaType.APPLICATION_FORM_URLENCODED); //
	  	
	  	// Obtention du Token d'acces a GraphAPI
	  	TokenResponse token = HttpClientHelper.callRestAPI(authHelperAAD.getConfiguration().getAccessGraphTokenUri(), HttpMethod.POST, null, TokenResponse.class, new GetTokenRequestPayload(HttpClientHelper.GRANT_TYPE_CREDENTIAL, app.getCertSecretValue(), app.getClientId(), authHelperAAD.getConfiguration().getMsGraphScope()), requestHeaders);
	  	requestHeaders.setBearerAuth(token.getAccess_token());
	  	
	  	// Creation d'un compte utilisateur via GraphAPI
	  	UserInfo user = HttpClientHelper.callRestAPI(authHelperAAD.getConfiguration().getMsGraphUsersEndpoint(), HttpMethod.POST, null, UserInfo.class, request, requestHeaders );
		
	  	// Retourne l'utilisateur cree
	  	return user;
	}

}
