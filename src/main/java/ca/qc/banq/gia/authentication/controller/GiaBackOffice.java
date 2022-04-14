/**
 *
 */
package ca.qc.banq.gia.authentication.controller;

import ca.qc.banq.gia.authentication.entities.App;
import ca.qc.banq.gia.authentication.models.AppPayload;

import java.util.List;


/**
 * Services web backoffice de gestion des applications BAnQ
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-12
 */
public interface GiaBackOffice {

    /**
     * Enregistre une App
     *
     * @param app
     * @return
     */
    String saveApp(App app);

    /**
     * Supprime une App
     * @param id
     */
    String deleteApp(String id);

    /**
     * Recherche la liste de toutes les applications enregistrees
     * @return
     */
    List<AppPayload> findAll();

    /**
     * Recherche une application a partir de son Id
     * @param id
     * @return
     */
    AppPayload findById(String id);

}
