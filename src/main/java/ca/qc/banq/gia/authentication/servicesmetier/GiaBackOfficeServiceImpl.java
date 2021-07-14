/**
 * 
 */
package ca.qc.banq.gia.authentication.servicesmetier;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ca.qc.banq.gia.authentication.config.TranslatorConfig;
import ca.qc.banq.gia.authentication.entities.App;
import ca.qc.banq.gia.authentication.entities.TypeAuth;
import ca.qc.banq.gia.authentication.exceptions.GIAException;
import ca.qc.banq.gia.authentication.helpers.HttpClientHelper;
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
	
	/*@Autowired
	PasswordEncoder passwordEncoder; */

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
		App saved = appRepo.findById(app.getClientId()).orElse(null);
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
	public String deleteApp(String id) {
		appRepo.deleteById(id);
		return translator.translate("app.deleted.successfull");
	}

	/*
	 * (non-javadoc)
	 * @see ca.qc.banq.gia.authentication.servicesmetier.GiaBackOfficeService#findAll()
	 */
	@Override
	public List<AppPayload> findAll() {
		return appRepo.findAll().stream().map(app -> app.toDTO( getLoginUrl(app.getClientId()), getRedirectApp(app.getTypeAuth()), getLogoutUrl(app.getClientId()) ) ).collect(Collectors.toList());
	}

	/*
	 * (non-javadoc)
	 * @see ca.qc.banq.gia.authentication.servicesmetier.GiaBackOfficeService#findById(java.lang.Long)
	 */
	@Override
	public AppPayload findById(String id) {
		App app = appRepo.findById(id).orElse(null);
		if(app == null) throw new GIAException("app.notfound");
		return app.toDTO( getLoginUrl(app.getClientId()), getRedirectApp(app.getTypeAuth()), getLogoutUrl(app.getClientId()) );
	}
	
	@Override
	public AppPayload findByClientId(String clientId) {
		App app = appRepo.findById(clientId).orElse(null);
		if(app == null) throw new GIAException("app.notfound");
		return app.toDTO( getLoginUrl(app.getClientId()), getRedirectApp(app.getTypeAuth()), getLogoutUrl(app.getClientId()) );
	}


	private String getLoginUrl(String id) {
		return serverHost.concat(servletPath).concat(HttpClientHelper.SIGNIN_ENDPOINT).concat("?" + HttpClientHelper.CLIENTID_PARAM + "=" + id );
	}

	private String getRedirectApp(TypeAuth typeauth) {
		return serverHost.concat(servletPath).concat(typeauth.equals(TypeAuth.B2C) ? HttpClientHelper.REDIRECTB2C_ENDPOINT : HttpClientHelper.REDIRECTAAD_ENDPOINT );
	}

	private String getLogoutUrl(String id) {
		return serverHost.concat(servletPath).concat(HttpClientHelper.SIGNOUT_ENDPOINT).concat("?" + HttpClientHelper.CLIENTID_PARAM + "=" + id );
	}

	@Override
	public List<AppPayload> findLikeTitle(String title) {
		return appRepo.findLikeTitle(title).stream().map(app -> app.toDTO( getLoginUrl(app.getClientId()), getRedirectApp(app.getTypeAuth()), getLogoutUrl(app.getClientId()) ) ).collect(Collectors.toList());
	}
}
