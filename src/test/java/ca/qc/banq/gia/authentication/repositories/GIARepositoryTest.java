package ca.qc.banq.gia.authentication.repositories;

import ca.qc.banq.gia.authentication.entities.App;
import ca.qc.banq.gia.authentication.entities.AuthenticationType;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static ca.qc.banq.gia.authentication.entities.AuthenticationType.AAD;
import static ca.qc.banq.gia.authentication.entities.AuthenticationType.B2C;

@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@DataJpaTest
class GIARepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private GIARepository giaRepository;

    @Test
    void findByTitle() {
        App app1 = buildAppEntity("68fb0e8c-7754-49ed-a7a7-7c084b9b7bd9",
                "idel-extranet-web-app",
                B2C,
                "http://localhost:8080/idel-local/extranet/redirectVersAccueilExtranet.seam",
                "gPpG_DVWH4KV9B42eoNZVOg7~p4-u8iQ-M",
                "B2C_1_signinsignup_idel_extranet"
        );
        entityManager.persist(app1);
        App app2 = buildAppEntity("e1ddda0b-7ce2-42e0-82c4-c849986f69e0",
                "idel-intranet-web-app",
                AAD,
                "http://localhost:8080/idel-local/intranet/IntranetAccueil.seam",
                "aRa~rBhcr_b1q6q3_W26g5fbyk~1sB629.",
                ""
        );
        entityManager.persist(app2);

        entityManager.flush();
        Assertions.assertThat(giaRepository.findByTitle("idel-intranet-web-app"))
                .usingRecursiveComparison()
                .isEqualTo(new PageImpl<>(Lists.list(app2)));
    }

    private App buildAppEntity(String clientId, String title, AuthenticationType authenticationType,
                               String homeUrl, String certSecretValue, String policySignUpSignIn) {
        return App.builder()
                .clientId(clientId)
                .title(title)
                .homeUrl(homeUrl)
                .authenticationType(authenticationType)
                .certSecretValue(certSecretValue)
                .policyEditProfile("")
                .policyResetPassword("")
                .policySignUpSignIn(policySignUpSignIn)
                .usersGroupId("")
                .nouveau(false)
                .build();

    }

}