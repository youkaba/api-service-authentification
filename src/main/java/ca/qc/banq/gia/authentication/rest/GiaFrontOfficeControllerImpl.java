/**
 * 
 */
package ca.qc.banq.gia.authentication.rest;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.qc.banq.gia.authentication.exceptions.GIAException;
import ca.qc.banq.gia.authentication.helpers.AuthHelperAAD;
import ca.qc.banq.gia.authentication.helpers.AuthHelperB2C;
import ca.qc.banq.gia.authentication.helpers.HttpClientHelper;
import ca.qc.banq.gia.authentication.models.AppPayload;
import ca.qc.banq.gia.authentication.models.CreateB2CUserRequestPayload;
import ca.qc.banq.gia.authentication.models.EditB2CUserRequestPayload;
import ca.qc.banq.gia.authentication.models.GetIdentitiesResponse;
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
	
	/*
	 * (non-javadoc)
	 * @see ca.qc.banq.gia.authentication.rest.GiaFrontOfficeController#createUserIntoAzureB2C(javax.servlet.http.HttpServletRequest, ca.qc.banq.gia.authentication.models.UserRequestPayload)
	 */
	@Override
    @PostMapping(HttpClientHelper.CREATEUSER_ENDPOINT)
    @ApiOperation("Cree un nouvel utilisateur dans Azure B2C")
	public UserInfo createUserIntoAzureB2C(@NotNull @RequestParam(HttpClientHelper.CLIENTID_PARAM) String appId, @RequestBody @Valid @NotNull(message = "invalid.createuser.request") CreateB2CUserRequestPayload request) throws Throwable {
		
		// Check params
		if(appId == null) throw new GIAException("invalid client_id");
		
		AppPayload app = business.findByClientId(appId);
		if(app == null) throw new GIAException("invalid client_id");
	  	
	  	// Obtention du Token d'acces a GraphAPI
	  	TokenResponse token = authHelperAAD.getAccessToken(app);
		
  		// Creation de l'utilisateur
  		UserInfo user = authHelperAAD.createUser(token, request.toUserRequestPayload(authHelperB2C.getConfiguration().getTenant()));

	  	// Ajout de l'utilisateur dans le groupe defini
	  	//if(app.getUsersGroupId() != null && !app.getUsersGroupId().isEmpty()) authHelperAAD.addUserTGroup(token, user.getId(), app.getUsersGroupId());
	  	
  		// Affectation de l'utilisateur a l'application
  		authHelperAAD.assignUserToApp(token, user.getId(), appId);
  		
	  	// Retourne l'utilisateur cree
	  	return user;
	}

	/*
	 * (non-javadoc)
	 * @see ca.qc.banq.gia.authentication.rest.GiaFrontOfficeController#editUserIntoAzureB2C(java.lang.String, ca.qc.banq.gia.authentication.models.EditB2CUserRequestPayload)
	 */
	@Override
    @PutMapping(HttpClientHelper.EDITUSER_ENDPOINT)
    @ApiOperation("Modifie un utilisateur dans Azure B2C")
	public void editUserIntoAzureB2C(@NotNull @RequestParam(HttpClientHelper.CLIENTID_PARAM) String appId, @RequestBody @Valid @NotNull(message = "invalid.createuser.request") EditB2CUserRequestPayload request) throws Throwable {

		AppPayload app = business.findByClientId(appId);
		if(app == null) throw new GIAException("invalid client_id");
	  	
	  	// Obtention du Token d'acces a GraphAPI
	  	TokenResponse token = authHelperAAD.getAccessToken(app);
		
  		// Recuperations des identities de l'utilisateur
	  	GetIdentitiesResponse identities = authHelperAAD.getUserIdentities(token, request.getId());
	  	
	  	// MAJ des identities de l'utilisateur
  		authHelperAAD.editUserIdentities(token, request.getId(), request.getPatchIdentitiesRequest(identities, authHelperB2C.getConfiguration().getTenant()));
	  	
  		// MAJ des infos de l'utilisateur
  		request.setUserPrincipalName((request.getUserPrincipalName().matches(HttpClientHelper.EMAIL_REGEX) ? StringUtils.replace(request.getUserPrincipalName(), "@", ".") : request.getUserPrincipalName()) .concat("@").concat(authHelperB2C.getConfiguration().getTenant()));
  		log.error("Request edit User = " + new ObjectMapper().writeValueAsString(request));
	  	authHelperAAD.editUser(token, request);
	}

}
