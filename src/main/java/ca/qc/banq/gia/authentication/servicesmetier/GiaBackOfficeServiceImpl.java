/**
 * 
 */
package ca.qc.banq.gia.authentication.servicesmetier;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.qc.banq.gia.authentication.config.TranslatorConfig;
import ca.qc.banq.gia.authentication.entities.App;
import ca.qc.banq.gia.authentication.exceptions.GIAException;
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
	TranslatorConfig translate;
	
	/*
	 * (non-javadoc)
	 * @see ca.qc.banq.gia.authentication.servicesmetier.GiaBackOfficeService#saveApp(ca.qc.banq.gia.authentication.entities.App)
	 */
	@Override
	public String saveApp(App app) {
		App saved = appRepo.findById(app.getId()).orElse(null);
		if(saved == null) app = appRepo.save(app);
		else saved.update(app);
		return translate.translate("app.saved.successfull");
	}

	/*
	 * (non-javadoc)
	 * @see ca.qc.banq.gia.authentication.servicesmetier.GiaBackOfficeService#deleteApp(java.lang.Long)
	 */
	@Override
	public String deleteApp(Long id) {
		appRepo.deleteById(id);
		return translate.translate("app.deleted.successfull");
	}

	/*
	 * (non-javadoc)
	 * @see ca.qc.banq.gia.authentication.servicesmetier.GiaBackOfficeService#findAll()
	 */
	@Override
	public List<App> findAll() {
		return appRepo.findAll();
	}

	/*
	 * (non-javadoc)
	 * @see ca.qc.banq.gia.authentication.servicesmetier.GiaBackOfficeService#findById(java.lang.Long)
	 */
	@Override
	public App findById(Long id) {
		App app = appRepo.findById(id).orElse(null);
		if(app == null) throw new GIAException("app.notfound");
		return app;
	}

}
