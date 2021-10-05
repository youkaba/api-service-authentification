/**
 * 
 */
package ca.qc.banq.gia.authentication.rest;

import ca.qc.banq.gia.authentication.models.CreateB2CUserRequestPayload;
import ca.qc.banq.gia.authentication.models.EditB2CUserRequestPayload;
import ca.qc.banq.gia.authentication.models.UserInfo;


/**
 * Services web backoffice de gestion des applications BAnQ
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-12
 */
public interface GiaFrontOfficeController {

	/**
	 * Creation d'un utilisateur dans Azure B2C
	 * @param httpRequest
	 * @param request
	 * @return
	 * @throws Throwable
	 */
	public UserInfo createUserIntoAzureB2C(String appId, CreateB2CUserRequestPayload request) throws Throwable;
	
	public void editUserIntoAzureB2C(String appId, EditB2CUserRequestPayload request) throws Throwable;
	
}
