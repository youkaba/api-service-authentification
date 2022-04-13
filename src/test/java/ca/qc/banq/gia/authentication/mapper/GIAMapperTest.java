package ca.qc.banq.gia.authentication.mapper;

import ca.qc.banq.gia.authentication.entities.App;
import ca.qc.banq.gia.authentication.entities.AuthenticationType;
import ca.qc.banq.gia.authentication.models.AppPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static ca.qc.banq.gia.authentication.entities.AuthenticationType.AAD;
import static ca.qc.banq.gia.authentication.entities.AuthenticationType.B2C;
import static org.assertj.core.api.Assertions.assertThat;

class GIAMapperTest {

    private static final String CLIENT_ID = "e1ddda0b-7ce2-42e0-82c4-c849986f69e0";
    private static final String TITLE = "idel-intranet-web-app";
    private static final String HOME_URL = "http://localhost:8080/idel-local/intranet/IntranetAccueil.seam";
    private static final AuthenticationType AUTHENTICATION_TYPE = AAD;
    private static final String CERT_SECRET = "aRa~rBhcr_b1q6q3_W26g5fbyk~1sB629.";
    private static final String POLICY_SIGNUP = "";
    private static final String SERVER_HOST = "server_host:port";
    private static final String LOGIN_URL = SERVER_HOST.concat("/gia/sign_in?appid=e1ddda0b-7ce2-42e0-82c4-c849986f69e0");
    private static final String LOGOUT_URL = SERVER_HOST.concat("/gia/sign_out?appid=e1ddda0b-7ce2-42e0-82c4-c849986f69e0");
    private static final String REDIRECTN_URL = SERVER_HOST.concat("/gia/redirect2_aad");

    private GIAMapper giaMapper;

    @BeforeEach
    void setUp() {
        giaMapper = Mappers.getMapper(GIAMapper.class);
    }

    @Test
    void toAppPayload() {
        assertThat(giaMapper.entityToAppPayload(buildAppEntity(),
                LOGIN_URL, REDIRECTN_URL, LOGOUT_URL))
                .usingRecursiveComparison()
                .isEqualTo(buildAppPayload(buildAppEntity()));

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