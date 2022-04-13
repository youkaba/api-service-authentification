package ca.qc.banq.gia.authentication.services;

import ca.qc.banq.gia.authentication.entities.App;
import ca.qc.banq.gia.authentication.models.AppPayload;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
 * Services metier backoffice de gestion des applications BAnQ
 *
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-12
 */
public interface GiaBackOfficeService {

    /**
     * Enregistre une App
     */
    String createApp(App app);

    App updateApp(App app);

    /**
     * Supprime une App
     */
    String deleteApp(String id);

    /**
     * Recherche la liste de toutes les applications enregistrees
     */
    List<AppPayload> findAll(String serverHost, String servletPath);

    /**
     * Recherche une application a partir de son Id
     */
    AppPayload findById(String id, String serverHost, String servletPath);

    @Transactional(readOnly = true)
    AppPayload findByClientId(String clientId, String serverHost, String servletPath);

    /**
     * Recherche par nom d'application
     */
    List<AppPayload> findByTitle(String title, String serverHost, String servletPath);

    AppPayload checkClientID(HttpServletRequest httpRequest, String serverHost, String servletPath);
}
