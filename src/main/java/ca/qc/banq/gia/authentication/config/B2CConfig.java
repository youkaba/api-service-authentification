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

	private String policySignUpSignIn;
	private String policyEditProfile;
	private String policyResetPassword;

	private String signUpSignInAuthority;
	private String editProfileAuthority;
	private String resetPasswordAuthority;

}
