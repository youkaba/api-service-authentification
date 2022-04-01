/**
 *
 */
package ca.qc.banq.gia.authentication.rest;

import ca.qc.banq.gia.authentication.exceptions.GIAException;
import ca.qc.banq.gia.authentication.helpers.AuthHelperAAD;
import ca.qc.banq.gia.authentication.helpers.AuthHelperB2C;
import ca.qc.banq.gia.authentication.helpers.HttpClientHelper;
import ca.qc.banq.gia.authentication.models.*;
import ca.qc.banq.gia.authentication.servicesmetier.GiaBackOfficeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-17
 */
@Slf4j
@RestController
@RequestMapping(HttpClientHelper.FRONTOFFICE_APIURL)
@Api("Services web front-office")
@RequiredArgsConstructor
public class GiaFrontOfficeControllerImpl {

    private final AuthHelperB2C authHelperB2C;
    private final AuthHelperAAD authHelperAAD;
    private final GiaBackOfficeService business;
    @Value("${server.host}")
    String serverHost;
    @Value("${server.servlet.context-path}")
    String servletPath;

    /*
     * (non-javadoc)
     * @see ca.qc.banq.gia.authentication.rest.GiaFrontOfficeController#createUserIntoAzureB2C(javax.servlet.http.HttpServletRequest, ca.qc.banq.gia.authentication.models.UserRequestPayload)
     */
    @PostMapping(HttpClientHelper.CREATEUSER_ENDPOINT)
    @ApiOperation("Cree un nouvel utilisateur dans Azure B2C")
    public UserInfo createUserIntoAzureB2C(@NotNull @RequestParam(HttpClientHelper.CLIENTID_PARAM) String appId, @RequestBody @Valid @NotNull(message = "invalid.createuser.request") CreateB2CUserRequestPayload request) {

        // Check params
        if (appId == null) throw new GIAException("invalid client_id");

        AppPayload app = business.findByClientId(appId);
        if (app == null) throw new GIAException("invalid client_id");

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
    @PutMapping(HttpClientHelper.EDITUSER_ENDPOINT)
    @ApiOperation("Modifie un utilisateur dans Azure B2C")
    public void editUserIntoAzureB2C(
            @NotNull
            @RequestParam(HttpClientHelper.CLIENTID_PARAM) String appId,
            @RequestBody
            @Valid
            @NotNull(message = "invalid.createuser.request")
                    EditB2CUserRequestPayload request) throws Throwable {

        AppPayload app = business.findByClientId(appId);
        if (app == null) throw new GIAException("invalid client_id");

        // Obtention du Token d'acces a GraphAPI
        TokenResponse token = authHelperAAD.getAccessToken(app);

        // Recuperations des identities de l'utilisateur
        GetIdentitiesResponse identities = authHelperAAD.getUserIdentities(token, request.getId());

        // MAJ des identities de l'utilisateur
        authHelperAAD.editUserIdentities(token, request.getId(), request.getPatchIdentitiesRequest(identities, authHelperB2C.getConfiguration().getTenant()));

        // MAJ des infos de l'utilisateur
        request.setUserPrincipalName((request.getUserPrincipalName().matches(HttpClientHelper.EMAIL_REGEX) ? StringUtils.replace(request.getUserPrincipalName(), "@", ".") : request.getUserPrincipalName()).concat("@").concat(authHelperB2C.getConfiguration().getTenant()));
        log.error("Request edit User = " + new ObjectMapper().writeValueAsString(request));
        authHelperAAD.editUser(token, request);
    }
}
