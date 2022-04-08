/**
 *
 */
package ca.qc.banq.gia.authentication.models;

import ca.qc.banq.gia.authentication.entities.AuthenticationType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.io.Serializable;

/**
 * Payload d'une application
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-12
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class AppPayload implements Serializable {

    private String clientId;
    private String title;
    private AuthenticationType authenticationType;
    private String homeUrl;
    private String certSecretValue;
    private String apiScope;
    private String loginURL;
    private String logoutURL;
    private String policySignUpSignIn;
    private String policyResetPassword;
    private String policyEditProfile;
    private String usersGroupId;
    private String redirectApp;

    @JsonIgnore
    public boolean isNouveau() {
        return this.clientId == null || this.clientId.isEmpty();
    }

    public String getHiddenCert() {
        return "*".repeat(certSecretValue.length());
    }
}
