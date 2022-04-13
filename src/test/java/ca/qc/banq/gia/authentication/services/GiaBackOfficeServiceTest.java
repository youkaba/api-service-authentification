package ca.qc.banq.gia.authentication.services;

import ca.qc.banq.gia.authentication.config.TranslatorConfig;
import ca.qc.banq.gia.authentication.entities.App;
import ca.qc.banq.gia.authentication.entities.AuthenticationType;
import ca.qc.banq.gia.authentication.exceptions.NotFoundException;
import ca.qc.banq.gia.authentication.mapper.GIAMapper;
import ca.qc.banq.gia.authentication.models.AppPayload;
import ca.qc.banq.gia.authentication.repositories.GIARepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static ca.qc.banq.gia.authentication.entities.AuthenticationType.AAD;
import static ca.qc.banq.gia.authentication.entities.AuthenticationType.B2C;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.util.Lists.list;
import static org.mockito.BDDMockito.doReturn;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GiaBackOfficeServiceTest {

    private static final String SERVER_HOST = "server_host:port";
    private static final String SERVER_PATH = "/gia";

    private static final String CLIENT_ID = "e1ddda0b-7ce2-42e0-82c4-c849986f69e0";
    private static final String TITLE = "idel-intranet-web-app";
    private static final String HOME_URL = "http://localhost:8080/idel-local/intranet/IntranetAccueil.seam";
    private static final AuthenticationType AUTHENTICATION_TYPE = AAD;
    private static final String CERT_SECRET = "aRa~rBhcr_b1q6q3_W26g5fbyk~1sB629.";
    private static final String POLICY_SIGNUP = "";
    private static final String LOGIN_URL = SERVER_HOST.concat("/gia/sign_in?appid=e1ddda0b-7ce2-42e0-82c4-c849986f69e0");
    private static final String LOGOUT_URL = SERVER_HOST.concat("/gia/sign_out?appid=e1ddda0b-7ce2-42e0-82c4-c849986f69e0");
    private static final String REDIRECTN_URL = SERVER_HOST.concat("/gia/redirect2_aad");


    private static final App APP_ENTITY = new App();
    private final AppPayload APP_PAYLOAD = buildAppPayload(buildAppEntity());

    @InjectMocks
    private GiaBackOfficeServiceImpl giaBackOfficeService;

    @Mock
    private GIARepository giaRepository;

    @Mock
    private GIAMapper giaMapper;

    @Mock
    private TranslatorConfig translatorConfig;

    @Test
    @DisplayName("create application")
    void createOrUpdateApp() {
        App application = buildAppEntity();
        when(giaRepository.findById(application.getClientId())).thenReturn(Optional.empty());
        when(giaRepository.save(application)).thenReturn(APP_ENTITY);
        when(translatorConfig.translate("app.saved.successfull")).thenReturn("Sauvegarde effectuée avec succès!");
        assertThat(giaBackOfficeService.createApp(application))
                .isSameAs("Sauvegarde effectuée avec succès!");
    }

    @Test
    @DisplayName("update application")
    void updateApp() {
        App application = buildAppEntity();

        when(giaRepository.findById(application.getClientId())).thenReturn(Optional.of(APP_ENTITY));
        when(giaRepository.save(application)).thenReturn(APP_ENTITY);
        when(translatorConfig.translate("app.updated.successfull")).thenReturn("Mise a jour effectuée avec succès!");

        assertThat(giaBackOfficeService.createApp(application))
                .isSameAs("Mise a jour effectuée avec succès!");
    }

    @Test
    @DisplayName("remove application")
    void deleteApp() {
        giaBackOfficeService.deleteApp(CLIENT_ID);
        verify(giaRepository).deleteById(CLIENT_ID);

    }

    @Test
    @DisplayName("client ID found")
    void findByClientId_Found() {
        App appEntity = buildAppEntity();
        when(giaRepository.findById(appEntity.getClientId())).thenReturn(Optional.of(appEntity));
        when(giaMapper.entityToAppPayload(appEntity, LOGIN_URL, REDIRECTN_URL, LOGOUT_URL))
                .thenReturn(APP_PAYLOAD);
        assertThat(giaBackOfficeService.findByClientId(appEntity.getClientId(), SERVER_HOST, SERVER_PATH))
                .isSameAs(APP_PAYLOAD);
    }

    @Test
    @DisplayName("client id not found")
    void findByClientId_NotFound() {
        when(giaRepository.findById(CLIENT_ID)).thenReturn(Optional.empty());
        when(translatorConfig.translate("app.notfound")).thenReturn("Application non trouvee");

        assertThatThrownBy(() -> giaBackOfficeService.findByClientId(CLIENT_ID, SERVER_HOST, SERVER_PATH))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Application non trouvee");
    }

    @Test
    void findAll() {
        App appEntity = buildAppEntity();

        when(giaRepository.findAll()).thenReturn(List.of(appEntity));
        doReturn(APP_PAYLOAD).when(giaMapper).entityToAppPayload(appEntity, LOGIN_URL, REDIRECTN_URL, LOGOUT_URL);

        assertThat(giaBackOfficeService.findAll(SERVER_HOST, SERVER_PATH))
                .usingRecursiveComparison()
                .isEqualTo(list(APP_PAYLOAD));
    }

    @Test
    void findByTitle() {
        App appEntity = buildAppEntity();

        given(giaRepository.findByTitle(appEntity.getTitle())).willReturn(List.of(appEntity));
        given(giaMapper.entityToAppPayload(appEntity, LOGIN_URL, REDIRECTN_URL, LOGOUT_URL))
                .willReturn(APP_PAYLOAD);

        assertThat(giaBackOfficeService.findByTitle(appEntity.getTitle(), SERVER_HOST, SERVER_PATH))
                .usingRecursiveComparison()
                .isEqualTo(list(APP_PAYLOAD));

    }

    private App buildAppEntity() {
        return App.builder()
                .clientId(CLIENT_ID)
                .title(TITLE)
                .homeUrl(HOME_URL)
                .authenticationType(AUTHENTICATION_TYPE)
                .certSecretValue(CERT_SECRET)
                .policyEditProfile("")
                .policyResetPassword("")
                .policySignUpSignIn(POLICY_SIGNUP)
                .usersGroupId("")
                .build();
    }

    private AppPayload buildAppPayload(App appEntity) {
        return AppPayload.builder()
                .clientId(appEntity.getClientId())
                .title(appEntity.getTitle())
                .authenticationType(appEntity.getAuthenticationType())
                .homeUrl(appEntity.getHomeUrl())
                .certSecretValue(appEntity.getCertSecretValue())
                .apiScope(appEntity.getAuthenticationType().equals(B2C) ? appEntity.getClientId() : "")
                .logoutURL(SERVER_HOST.concat("/gia/sign_out?appid=e1ddda0b-7ce2-42e0-82c4-c849986f69e0"))
                .loginURL(SERVER_HOST.concat("/gia/sign_in?appid=e1ddda0b-7ce2-42e0-82c4-c849986f69e0"))
                .policySignUpSignIn(appEntity.getPolicySignUpSignIn())
                .policyEditProfile(appEntity.getPolicyEditProfile())
                .policyResetPassword(appEntity.getPolicyResetPassword())
                .usersGroupId(appEntity.getUsersGroupId())
                .redirectApp(SERVER_HOST.concat("/gia/redirect2_aad"))
                .build();
    }
}