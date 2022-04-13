package ca.qc.banq.gia.authentication.services;

import ca.qc.banq.gia.authentication.config.TranslatorConfig;
import ca.qc.banq.gia.authentication.entities.App;
import ca.qc.banq.gia.authentication.entities.AuthenticationType;
import ca.qc.banq.gia.authentication.exceptions.NotFoundException;
import ca.qc.banq.gia.authentication.mapper.GIAMapper;
import ca.qc.banq.gia.authentication.models.AppPayload;
import ca.qc.banq.gia.authentication.repositories.GIARepository;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Transactional
@Slf4j
public class GiaBackOfficeServiceImpl implements GiaBackOfficeService {

    private final GIARepository giaRepository;
    private final TranslatorConfig translator;
    private final GIAMapper giaMapper;


//	@Autowired
//	PasswordEncoder passwordEncoder;


    /*
     * (non-javadoc)
     * @see ca.qc.banq.gia.authentication.servicesmetier.GiaBackOfficeService#saveApp(ca.qc.banq.gia.authentication.entities.App)
     */
    @Override
    public String createApp(App app) {
        Optional<App> saved = giaRepository.findById(app.getClientId());
        if (saved.isPresent()) {
            App newApp = this.updateApp(app);
            giaRepository.save(newApp);
            return translator.translate("app.updated.successfull");
        }

        giaRepository.save(app);
        return translator.translate("app.saved.successfull");
    }

    @Override
    @JsonIgnore
    public App updateApp(App app) {
        return App.builder()
                .clientId(app.getClientId())
                .title(app.getTitle())
                .homeUrl(app.getHomeUrl())
                .certSecretValue(app.getCertSecretValue())
                .authenticationType(app.getAuthenticationType())
                .policyEditProfile(app.getPolicyEditProfile())
                .policySignUpSignIn(app.getPolicySignUpSignIn())
                .policyResetPassword(app.getPolicyResetPassword())
                .usersGroupId(app.getUsersGroupId())
                .build();
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
    public List<AppPayload> findAll(String serverHost, String servletPath) {
        return giaRepository.findAll().stream()
                .map(giaEntity -> giaMapper.entityToAppPayload(giaEntity,
                        getLoginOrLogoutUrl(giaEntity.getClientId(), serverHost, servletPath, SIGNIN_ENDPOINT),
                        getRedirectApp(giaEntity.getAuthenticationType(), serverHost, servletPath),
                        getLoginOrLogoutUrl(giaEntity.getClientId(), serverHost, servletPath, SIGNOUT_ENDPOINT)))
                .toList();
    }

    /*
     * (non-javadoc)
     * @see ca.qc.banq.gia.authentication.servicesmetier.GiaBackOfficeService#findById(java.lang.Long)
     */
    @Override
    @Transactional(readOnly = true)
    public AppPayload findById(String id, String serverHost, String servletPath) {
        return giaRepository.findById(id)
                .map(giaEntity -> giaMapper.entityToAppPayload(giaEntity,
                        getLoginOrLogoutUrl(giaEntity.getClientId(), serverHost, servletPath, SIGNIN_ENDPOINT),
                        getRedirectApp(giaEntity.getAuthenticationType(), serverHost, servletPath),
                        getLoginOrLogoutUrl(giaEntity.getClientId(), serverHost, servletPath, SIGNOUT_ENDPOINT)))
                .orElseThrow(() -> new NotFoundException(translator.translate("app.notfound")));

    }

    @Override
    @Transactional(readOnly = true)
    public AppPayload findByClientId(String clientId, String serverHost, String servletPath) {
        return giaRepository.findById(clientId)
                .map(giaEntity ->
                        giaMapper.entityToAppPayload(giaEntity,
                                getLoginOrLogoutUrl(giaEntity.getClientId(), serverHost, servletPath, SIGNIN_ENDPOINT),
                                getRedirectApp(giaEntity.getAuthenticationType(), serverHost, servletPath),
                                getLoginOrLogoutUrl(giaEntity.getClientId(), serverHost, servletPath, SIGNOUT_ENDPOINT)))
                .orElseThrow(() -> new NotFoundException(translator.translate("app.notfound")));
    }

    private String getLoginOrLogoutUrl(String id, String serverHost, String servletPath, String endPoint) {
        return serverHost.concat(servletPath)
                .concat(endPoint)
                .concat("?" + CLIENTID_PARAM + "=" + id);
    }


    private String getRedirectApp(AuthenticationType authenticationType, String serverHost, String servletPath) {
        return serverHost.concat(servletPath)
                .concat(authenticationType.equals(B2C) ? REDIRECTB2C_ENDPOINT : REDIRECTAAD_ENDPOINT);
    }

    @Transactional(readOnly = true)
    public List<AppPayload> findByTitle(String title, String serverHost, String servletPath) {
        return giaRepository.findByTitle(title)
                .stream()
                .map(giaEntity -> giaMapper.entityToAppPayload(giaEntity,
                        getLoginOrLogoutUrl(giaEntity.getClientId(), serverHost, servletPath, SIGNIN_ENDPOINT),
                        getRedirectApp(giaEntity.getAuthenticationType(), serverHost, servletPath),
                        getLoginOrLogoutUrl(giaEntity.getClientId(), serverHost, servletPath, SIGNOUT_ENDPOINT)))
                .toList();
    }

    @Override
    public AppPayload checkClientID(HttpServletRequest httpRequest, String serverHost, String servletPath) {
        // Recuperation de l'id de l'application dans la requete
        Optional<String> clientId = Optional.ofNullable(httpRequest.getParameter(CLIENTID_PARAM));
        return clientId
                .map(id -> findByClientId(id, serverHost, servletPath))
                .orElseThrow(() -> new NotFoundException("unable to find appid"));
    }

}
