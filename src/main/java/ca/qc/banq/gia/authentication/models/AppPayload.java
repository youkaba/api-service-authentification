/**
 * 
 */
package ca.qc.banq.gia.authentication.models;

import java.io.Serializable;

import ca.qc.banq.gia.authentication.entities.TypeAuth;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Payload d'une application
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-12
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@SuppressWarnings("serial")
public class AppPayload implements Serializable {

	String clientId;
	String title;
	TypeAuth typeAuth;
	String homeUrl;
	String certSecretValue;
	String apiScope;
	String loginURL;
	String policySignUpSignIn;
	String policyResetPassword;
	String policyEditProfile;
	String redirectApp;
}
