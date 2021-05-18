/**
 * 
 */
package ca.qc.banq.gia.authentication.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.qc.banq.gia.authentication.models.UserInfo;


/**
 * Services web backoffice de gestion des applications BAnQ
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-12
 */
public interface GiaFrontOfficeController {

	public UserInfo getConnectedUser(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Throwable;
	public void signOut(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Throwable;
}
