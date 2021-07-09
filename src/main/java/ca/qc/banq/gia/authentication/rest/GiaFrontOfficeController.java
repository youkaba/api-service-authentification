/**
 * 
 */
package ca.qc.banq.gia.authentication.rest;

import javax.servlet.http.HttpServletRequest;

import ca.qc.banq.gia.authentication.models.CreateUserRequestPayload;
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
	public UserInfo createUserIntoAzureB2C(HttpServletRequest httpRequest, CreateUserRequestPayload request) throws Throwable;
	
}
