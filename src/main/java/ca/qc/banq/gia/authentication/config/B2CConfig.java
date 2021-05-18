/**
 * 
 */
package ca.qc.banq.gia.authentication.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * Configuration B2C
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-10
 */
@Data
@Component
@ConfigurationProperties("b2c")
public class B2CConfig {

	private String host;
	private String tenant;
	private String authorityBase;

	/** Retourne l'url B2C du flux de connexion */
	public String getSignUpSignInAuthority(String policySignIn) {
		return this.authorityBase.concat(policySignIn);
	}
	
	/** Retourne l'url B2C du flux d'edition du profil usager */
	public String getEditProfileAuthority(String policyEditProfile) {
		return this.authorityBase.concat(policyEditProfile);
	}

	/** Retourne l'url B2C du flux de reinitialisation de mot de passe */
	public String getResetPasswordAuthority(String policyResetPwd) {
		return this.authorityBase.concat(policyResetPwd);
	}
}
