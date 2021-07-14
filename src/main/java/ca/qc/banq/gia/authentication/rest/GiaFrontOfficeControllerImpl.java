/**
 * 
 */
package ca.qc.banq.gia.authentication.rest;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.qc.banq.gia.authentication.exceptions.GIAException;
import ca.qc.banq.gia.authentication.helpers.AuthHelperAAD;
import ca.qc.banq.gia.authentication.helpers.AuthHelperB2C;
import ca.qc.banq.gia.authentication.helpers.HttpClientHelper;
import ca.qc.banq.gia.authentication.models.AppPayload;
import ca.qc.banq.gia.authentication.models.TokenResponse;
import ca.qc.banq.gia.authentication.models.UserInfo;
import ca.qc.banq.gia.authentication.models.UserRequestPayload;
import ca.qc.banq.gia.authentication.servicesmetier.GiaBackOfficeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-17
 */
@RestController
@RequestMapping(HttpClientHelper.FRONTOFFICE_APIURL)
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
	public UserInfo createUserIntoAzureB2C(HttpServletRequest httpRequest, UserRequestPayload request) throws Throwable {
		
		// Recuperation des parametres d'entete de la requete
		String appId = httpRequest.getParameter(HttpClientHelper.CLIENTID_PARAM) ;
		
		// Check params
		if(appId == null) throw new GIAException("invalid client_id");
		
		AppPayload app = business.findByClientId(appId);
		if(app == null) throw new GIAException("invalid client_id");
	  	
	  	// Obtention du Token d'acces a GraphAPI
	  	TokenResponse token = authHelperAAD.getAccessToken(app);
		
  		// Construction des identities de l'utilisateur
  		request.buildIdentities(authHelperB2C.getConfiguration().getTenant());
  		
  		// Creation de l'utilisateur
  		UserInfo user = authHelperAAD.createUser(token, request);

	  	// Ajout de l'utilisateur dans le groupe defini
	  	if(app.getUsersGroupId() != null && !app.getUsersGroupId().isEmpty()) authHelperAAD.addUserTGroup(token, user.getId(), app.getUsersGroupId());
	  	
	  	// Retourne l'utilisateur cree
	  	return user;
	}

}
