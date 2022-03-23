/**
 * 
 */
package ca.qc.banq.gia.authentication.servicesmetier;

import ca.qc.banq.gia.authentication.entities.App;
import ca.qc.banq.gia.authentication.models.AppPayload;

import java.util.List;


/**
 * Services metier backoffice de gestion des applications BAnQ
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-12
 */
public interface GiaBackOfficeService {

	/**
	 * Enregistre une App
	 *
	 * @param app
	 */
	default String saveApp(App app) {
		return null;
	}

	/**
	 * Supprime une App
	 * @param id
	 */
	String deleteApp(String id);
	
	/**
	 * Recherche la liste de toutes les applications enregistrees
	 */
	List<AppPayload> findAll();

	/**
	 * Recherche une application a partir de son Id
	 * @param id
	 */
	AppPayload findById(String id);
	
	/**
	 * 
	 * @param clientId
	 */
	AppPayload findByClientId(String clientId);
	
	/**
	 * Recherche par nom d'application
	 * @param title
	 */
	List<AppPayload> findLikeTitle(String title);
}
