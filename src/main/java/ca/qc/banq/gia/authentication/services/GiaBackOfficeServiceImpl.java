package ca.qc.banq.gia.authentication.services;

import ca.qc.banq.gia.authentication.config.TranslatorConfig;
import ca.qc.banq.gia.authentication.entities.App;
import ca.qc.banq.gia.authentication.entities.AuthenticationType;
import ca.qc.banq.gia.authentication.exceptions.GIAException;
import ca.qc.banq.gia.authentication.mapper.GIAMapper;
import ca.qc.banq.gia.authentication.models.AppPayload;
import ca.qc.banq.gia.authentication.repositories.GIARepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

import static ca.qc.banq.gia.authentication.entities.AuthenticationType.B2C;
import static ca.qc.banq.gia.authentication.helpers.HttpClientHelper.*;


/**
 * Implementation des Services metier backoffice de gestion des applications BAnQ
 *
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-12
 */
@Service
@RequiredArgsConstructor
public class GiaBackOfficeServiceImpl implements GiaBackOfficeService {

    private final GIARepository giaRepository;

    private final TranslatorConfig translator;

    private final GIAMapper giaMapper;
	
	/*@Autowired
	PasswordEncoder passwordEncoder; */

    @Value("${server.host}")
    private String serverHost;

    @Value("${server.servlet.context-path}")
    private String servletPath;

    /*
     * (non-javadoc)
     * @see ca.qc.banq.gia.authentication.servicesmetier.GiaBackOfficeService#saveApp(ca.qc.banq.gia.authentication.entities.App)
     */
    @Override
    public String saveApp(App app) {
        App saved = giaRepository.findById(app.getClientId()).orElse(null);

        if (saved != null) {
            saved.update(app);
            giaRepository.save(saved);
        } else {
            giaRepository.save(app);
        }
        return translator.translate("app.saved.successfull");
    }

    /*
     * (non-javadoc)
     * @see ca.qc.banq.gia.authentication.servicesmetier.GiaBackOfficeService#deleteApp(java.lang.Long)
     */
    @Override
    public String deleteApp(String id) {
        giaRepository.deleteById(id);
        return translator.translate("app.deleted.successfull");
    }

    /*
     * (non-javadoc)
     * @see ca.qc.banq.gia.authentication.servicesmetier.GiaBackOfficeService#findAll()
     */
    @Override
    @Transactional(readOnly = true)
    public List<AppPayload> findAll() {
        return giaRepository.findAll().stream()
                .map(giaEntity -> giaMapper.entityToAppPayload(giaEntity, getLoginUrl(giaEntity.getClientId()),
                        getRedirectApp(giaEntity.getAuthenticationType()),
                        getLogoutUrl(giaEntity.getClientId())))
                .toList();
    }

    /*
     * (non-javadoc)
     * @see ca.qc.banq.gia.authentication.servicesmetier.GiaBackOfficeService#findById(java.lang.Long)
     */
    @Override
    @Transactional(readOnly = true)
    public AppPayload findById(String id) {
        return giaRepository.findById(id)
                .map(giaEntity -> giaMapper.entityToAppPayload(giaEntity, getLoginUrl(giaEntity.getClientId()),
                        getRedirectApp(giaEntity.getAuthenticationType()),
                        getLogoutUrl(giaEntity.getClientId())))
                .orElseThrow(() -> new GIAException("app.notfound"));

    }

    @Override
    @Transactional(readOnly = true)
    public AppPayload findByClientId(String clientId) {
        return giaRepository.findById(clientId)
                .map(giaEntity -> giaMapper.entityToAppPayload(giaEntity, getLoginUrl(giaEntity.getClientId()),
                        getRedirectApp(giaEntity.getAuthenticationType()),
                        getLogoutUrl(giaEntity.getClientId())))
                .orElseThrow(() -> new GIAException("app.notfound"));
    }


    private String getLoginUrl(String id) {
        return serverHost.concat(servletPath)
                .concat(SIGNIN_ENDPOINT)
                .concat("?" + CLIENTID_PARAM + "=" + id);
    }

    private String getRedirectApp(AuthenticationType authenticationType) {
        return serverHost.concat(servletPath)
                .concat(authenticationType.equals(B2C) ? REDIRECTB2C_ENDPOINT : REDIRECTAAD_ENDPOINT);
    }

    private String getLogoutUrl(String id) {
        return serverHost.concat(servletPath).concat(SIGNOUT_ENDPOINT).concat("?" + CLIENTID_PARAM + "=" + id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppPayload> findLikeTitle(String title) {
        return giaRepository.findLikeTitle(title)
                .stream()
                .map(giaEntity -> giaMapper.entityToAppPayload(giaEntity,
                        getLoginUrl(giaEntity.getClientId()),
                        getRedirectApp(giaEntity.getAuthenticationType()),
                        getLogoutUrl(giaEntity.getClientId())))
                .toList();
    }

    @Override
    public AppPayload checkClientID(HttpServletRequest httpRequest) {
        // Recuperation de l'id de l'application dans la requete
        Optional<String> clientId = Optional.ofNullable(httpRequest.getParameter(CLIENTID_PARAM));
        return clientId
                .map(this::findByClientId)
                .orElseThrow(() -> new GIAException("unable to find appid"));
    }
}
