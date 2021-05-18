/**
 * 
 */
package ca.qc.banq.gia.authentication.config;

import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.ContextLoaderListener;

import ca.qc.banq.gia.authentication.entities.App;
import ca.qc.banq.gia.authentication.entities.TypeAuth;
import ca.qc.banq.gia.authentication.repositories.AppRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * Service d'initialisation de l'application
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @version 1.0
 * @since 2018-08-27
 */
@Slf4j
@Component
@WebListener
public class ListenerConfig extends ContextLoaderListener {

	@Autowired
	AppRepository appRepo;
	
	/**
	 * Initialisation d'une liste dapplications par defaut pour des tests
	 */
	App[] initApps = new App[] {
			new App(null, "msal-b2c-web-sample", TypeAuth.B2C, "http://localhost:9090/gia/v1/auth/b2c", "68fb0e8c-7754-49ed-a7a7-7c084b9b7bd9", "gPpG_DVWH4KV9B42eoNZVOg7~p4-u8iQ-M", "B2C_1_signupsignin1", "", ""),
			new App(null, "msal-web-sample", TypeAuth.AAD, "http://localhost:9090/gia/v1/auth/aad", "68fb0e8c-7754-49ed-a7a7-7c084b9b7bd9", "gPpG_DVWH4KV9B42eoNZVOg7~p4-u8iQ-M", "B2C_1_signupsignin1", "", "")
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
	private void initApp() {
		try {
			
			for(App app : initApps) {
				if(appRepo.findByClientId(app.getClientId()).isEmpty()) appRepo.save(app);
			}
			
		} catch(Exception e) {
			log.error(" initialization has failed because of " + e.getMessage());
		}
	}
	
}
