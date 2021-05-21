/**
 * 
 */
package ca.qc.banq.gia.authentication.rest;

import java.util.List;

import ca.qc.banq.gia.authentication.entities.App;
import ca.qc.banq.gia.authentication.models.AppPayload;


/**
 * Services web backoffice de gestion des applications BAnQ
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-12
 */
public interface GiaBackOfficeController {

	/**
	 * Enregistre une App
	 * @param app
	 * @return
	 */
	public String saveApp(App app);
	
	/**
	 * Supprime une App
	 * @param id
	 */
	public String deleteApp(String id);
	
	/**
	 * Recherche la liste de toutes les applications enregistrees
	 * @return
	 */
	public List<AppPayload> findAll();

	/**
	 * Recherche une application a partir de son Id
	 * @param id
	 * @return
	 */
	public AppPayload findById(String id);
	
}
