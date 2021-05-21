/**
 * 
 */
package ca.qc.banq.gia.authentication.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ca.qc.banq.gia.authentication.entities.App;

/**
 * Referentiel de l'entite App
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-12
 */
@Repository
public interface AppRepository extends JpaRepository<App, String> {

	List<App> findByTitle(String title);
}
