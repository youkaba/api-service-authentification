/**
 *
 */
package ca.qc.banq.gia.authentication.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration B2C
 *
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-10
 */
@Data
@Component
@ConfigurationProperties("b2c")
public class AzureB2CConfig {

    private String host;
    private String tenant;
    private String authorityBase;

    /**
     * Retourne l'url B2C du flux de connexion
     */
    public String getSignUpSignInAuthority(String policySignIn) {
        return this.authorityBase.concat(policySignIn).concat("/");
    }

    /**
     * Retourne l'url B2C du flux d'edition du profil usager
     */
    public String getEditProfileAuthority(String policyEditProfile) {
        return this.authorityBase.concat(policyEditProfile).concat("/");
    }

    /**
     * Retourne l'url B2C du flux de reinitialisation de mot de passe
     */
    public String getResetPasswordAuthority(String policyResetPwd) {
        return this.authorityBase.concat(policyResetPwd).concat("/");
    }
}
