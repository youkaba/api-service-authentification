/**
 *
 */
package ca.qc.banq.gia.authentication.models;

import ca.qc.banq.gia.authentication.helpers.HttpClientHelper;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * @author francis.djiomou
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EditB2CUserRequestPayload implements Serializable {

    @NotNull(message = "EditB2CUserRequestPayload.id.NotNull")
    @ApiModelProperty(required = true)
    private String id;

    @NotNull(message = "EditB2CUserRequestPayload.userPrincipalName.NotNull")
    @ApiModelProperty(required = true, value = "userPrincipalName", example = "MelissaD@banq.qc.ca")
    private String userPrincipalName;

    @NotNull(message = "EditB2CUserRequestPayload.displayName.NotNull")
    @ApiModelProperty(required = true, value = "displayName", example = "Melissa Darrow")
    private String displayName;

    @NotNull(message = "EditB2CUserRequestPayload.mail.NotNull")
    @ApiModelProperty(value = "mail", example = "MelissaD@yahoo.com")
    private String mail;

    @ApiModelProperty(value = "mobilePhone", example = "+1 206 555 0110")
    private String mobilePhone;


    public GetIdentitiesResponse getPatchIdentitiesRequest(GetIdentitiesResponse identities, String tenant) {
        //boolean principalIsMail = this.userPrincipalName.equalsIgnoreCase(this.mail);
        boolean principalIsAnEmail = this.userPrincipalName.matches(HttpClientHelper.EMAIL_REGEX);

        // Parcours des identites de l'utilisateur
        identities.value().forEach(id -> {
            // Si l'identite courante est un userName, on met a jour son id
            if (id.getSignInType().equals(SignInType.USERNAME.getValue()) && !principalIsAnEmail)
                id.setIssuerAssignedId(this.userPrincipalName);
            if (id.getSignInType().equals(SignInType.EMAIL.getValue())) id.setIssuerAssignedId(this.mail);
            //if(id.getSignInType()()().equals(SignInType.FEDERATED.getValue()) && mobilePhone != null && !mobilePhone.isEmpty()) id.setIssuerAssignedId(this.mobilePhone);
            if (id.getSignInType().equals(SignInType.PRINCIPALNAME.getValue())) {
                id.setIssuerAssignedId((principalIsAnEmail ? StringUtils.replace(this.userPrincipalName, "@", ".") : this.userPrincipalName) + ("@" + tenant));
            }
        });

        // Si les identities ne contiennent pas de username, on ajoute un username
        if (identities.value().stream().noneMatch(id -> id.getSignInType().equalsIgnoreCase(SignInType.USERNAME.getValue()))
                && !principalIsAnEmail)
            identities.value().add(new IdentityPayload(SignInType.USERNAME.getValue(), tenant, this.userPrincipalName));
        // Si les identities ne contiennent pas de mail, on ajoute un mail
        if (identities.value().stream().noneMatch(id -> id.getSignInType().equalsIgnoreCase(SignInType.EMAIL.getValue())))
            identities.value().add(new IdentityPayload(SignInType.EMAIL.getValue(), tenant, this.mail));
        // Si les identities ne contiennent pas de federated, on ajoute un telephone
        if (isNotEmpty(mobilePhone) &&
                identities.value().stream().noneMatch(id -> id.getSignInType().equals(SignInType.USERNAME.getValue())))
            identities.value().add(new IdentityPayload(SignInType.USERNAME.getValue(), tenant, this.mobilePhone));

        return identities;
    }
}
