/**
 * 
 */
package ca.qc.banq.gia.authentication.servicesmetier;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import ca.qc.banq.gia.authentication.config.TranslatorConfig;
import ca.qc.banq.gia.authentication.entities.App;
import ca.qc.banq.gia.authentication.exceptions.GIAException;
import ca.qc.banq.gia.authentication.filter.AuthFilter;
import ca.qc.banq.gia.authentication.models.AppPayload;
import ca.qc.banq.gia.authentication.repositories.AppRepository;


/**
 * Implementation des Services metier backoffice de gestion des applications BAnQ
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-12
 */
@Service
public class GiaBackOfficeServiceImpl implements GiaBackOfficeService {

	@Autowired
	AppRepository appRepo;
	
	@Autowired
	TranslatorConfig translator;

	@Value("${server.host}")
	String serverHost;
	
	@Value("${server.servlet.context-path}")
	String servletPath;
	
	/*
	 * (non-javadoc)
	 * @see ca.qc.banq.gia.authentication.servicesmetier.GiaBackOfficeService#saveApp(ca.qc.banq.gia.authentication.entities.App)
	 */
	@Override
	public String saveApp(App app) {
		App saved = appRepo.findById(app.getId()).orElse(null);
		if(saved == null) app = appRepo.save(app);
		else {
			saved.update(app);
			appRepo.save(saved);
		}
		return translator.translate("app.saved.successfull");
	}

	/*
	 * (non-javadoc)
	 * @see ca.qc.banq.gia.authentication.servicesmetier.GiaBackOfficeService#deleteApp(java.lang.Long)
	 */
	@Override
	public String deleteApp(Long id) {
		appRepo.deleteById(id);
		return translator.translate("app.deleted.successfull");
	}

	/*
	 * (non-javadoc)
	 * @see ca.qc.banq.gia.authentication.servicesmetier.GiaBackOfficeService#findAll()
	 */
	@Override
	public List<AppPayload> findAll() {
		return appRepo.findAll().stream().map(app -> app.toDTO( getContextPath(app.getId()), getRedirectApp() ) ).collect(Collectors.toList());
	}

	/*
	 * (non-javadoc)
	 * @see ca.qc.banq.gia.authentication.servicesmetier.GiaBackOfficeService#findById(java.lang.Long)
	 */
	@Override
	public AppPayload findById(Long id) {
		App app = appRepo.findById(id).orElse(null);
		if(app == null) throw new GIAException("app.notfound");
		return app.toDTO( getContextPath(app.getId()), getRedirectApp() );
	}
	
	@Override
	public AppPayload findByClientId(String clientId) {
		App app = appRepo.findByClientId(clientId).stream().findFirst().orElse(null);
		if(app == null) throw new GIAException("app.notfound");
		return app.toDTO( getContextPath(app.getId()), getRedirectApp() );
	}


	private String getContextPath(Long id) {
		return serverHost.concat(servletPath).concat("?" + AuthFilter.APP_ID + "=" + id );
		/* try {
			return ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString().concat("?" + AuthFilter.APP_ID + "=" + id );
		}catch(Exception e) {return "";} */
	}

	private String getRedirectApp() {
		return serverHost.concat(servletPath).concat("/redirect_app" );
		/*try {
			
		}catch(Exception e) {return "";} */
	}
}
