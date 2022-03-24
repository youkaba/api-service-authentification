/**
 * 
 */
package ca.qc.banq.gia.authentication.config;

import ca.qc.banq.gia.authentication.entities.App;
import ca.qc.banq.gia.authentication.entities.TypeAuth;
import ca.qc.banq.gia.authentication.repositories.AppRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.ContextLoaderListener;

import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;
import java.util.Arrays;

import static java.util.function.Predicate.not;

/**
 * Service d'initialisation de l'application
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @version 1.0
 * @since 2018-08-27
 */
@Slf4j
@Component
@WebListener
@RequiredArgsConstructor
@Profile("local")
public class ListenerConfig extends ContextLoaderListener {

	private final AppRepository appRepo;
	
	/**
	 * Initialisation d'une liste dapplications par defaut pour des tests
	 */
	App[] initApps = new App[] {
			new App("68fb0e8c-7754-49ed-a7a7-7c084b9b7bd9", "idel-extranet-web-app", TypeAuth.B2C, "http://localhost:8080/idel-local/extranet/redirectVersAccueilExtranet.seam", "gPpG_DVWH4KV9B42eoNZVOg7~p4-u8iQ-M", "B2C_1_signinsignup_idel_extranet", "", "", "",false),
			new App("e1ddda0b-7ce2-42e0-82c4-c849986f69e0", "idel-intranet-web-app", TypeAuth.AAD, "http://localhost:8080/idel-local/intranet/IntranetAccueil.seam", "aRa~rBhcr_b1q6q3_W26g5fbyk~1sB629.", "", "", "", "",false)
	};
	
	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		initApp();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {}


	/**
	 * Initialisation des donnees
	 */
	@Transactional
	void initApp() {
		try {

			Arrays.stream(initApps)
					.filter(not(app -> appRepo.existsById(app.getClientId())))
					.forEach(appRepo::save);
		} catch (Exception e) {
			log.error(" initialization has failed because of " + e.getMessage());
		}
	}
	
}
