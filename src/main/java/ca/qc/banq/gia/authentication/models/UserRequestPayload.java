package ca.qc.banq.gia.authentication.models;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.*;

/**
 * Payload de la requete de creation d'un nouvel utilisateur
 *
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestPayload implements Serializable {


    @ApiModelProperty(value = "mobilePhone", example = "+1 206 555 0110")
    private String mobilePhone;

    @NotNull(message = "CreateUserRequestPayload.userPrincipalName.NotNull")
    @ApiModelProperty(required = true, value = "userPrincipalName", example = "MelissaD@banq.qc.ca")
    private String userPrincipalName;

    @ApiModelProperty(value = "identities", example = "[]")
    private List<IdentityPayload> identities;

    @NotNull(message = "CreateUserRequestPayload.mail.NotNull")
    @ApiModelProperty(value = "mail", example = "MelissaD@yahoo.com")
    private String mail;


    @NotNull(message = "CreateUserRequestPayload.accountEnabled.NotNull")
    @ApiModelProperty(required = true, value = "accountEnabled", example = "true")
    private boolean accountEnabled = true;


    @ApiModelProperty(value = "city", example = "Seattle")
    private String city;


    @ApiModelProperty(value = "country", example = "United States")
    private String country;


    @ApiModelProperty(value = "department", example = "Sales & Marketing")
    private String department;

    @NotNull(message = "CreateUserRequestPayload.displayName.NotNull")
    @ApiModelProperty(required = true, value = "displayName", example = "Melissa Darrow")
    private String displayName;


    @ApiModelProperty(value = "givenName", example = "Melissa")
    private String givenName;


    @ApiModelProperty(value = "jobTitle", example = "Marketing Director")
    private String jobTitle;


    //@NotNull(message = "CreateUserRequestPayload.mailNickname.NotNull")
    @ApiModelProperty(value = "mailNickname", example = "MelissaD")
    private String mailNickname;


    @NotNull(message = "CreateUserRequestPayload.passwordPolicies.NotNull")
    @ApiModelProperty(value = "passwordProfile", example = "\"passwordProfile\": {\"password\": \"87f312a1-38ec-1179-c230-bbd2ab1d0d9c\",\"forceChangePasswordNextSignIn\": false}")
    private PasswordPolicy passwordProfile;


    @ApiModelProperty(value = "officeLocation", example = "131/1105")
    private String officeLocation;


    @ApiModelProperty(value = "postalCode", example = "98052")
    private String postalCode;


    @ApiModelProperty(value = "preferredLanguage", example = "en-US")
    private String preferredLanguage;


    @ApiModelProperty(value = "state", example = "WA")
    private String state;


    @ApiModelProperty(value = "streetAddress", example = "9256 Towne Center Dr., Suite 400")
    private String streetAddress;


    @ApiModelProperty(value = "surname", example = "Darrow")
    private String surname;


    @ApiModelProperty(value = "usageLocation", example = "US")
    private String usageLocation;

    /**
     * Construction des identities de l'utilisateur
     */
    public void buildIdentities(String tenant) {

        // Initialisation
        this.identities = new ArrayList<>();
        if (this.state != null && (this.state.isEmpty() || this.state.isBlank())) this.state = null;
        if (this.usageLocation != null && (this.usageLocation.isEmpty() || this.usageLocation.isBlank()))
            this.usageLocation = null;
        if (this.streetAddress != null && (this.streetAddress.isEmpty() || this.streetAddress.isBlank()))
            this.streetAddress = null;

        // Ajout de l'adresse mail comme identifiant de connexion
        if (isNotEmpty(this.mail)) {
            identities.add(new IdentityPayload(SignInType.EMAIL.getValue(), tenant, this.mail));
            if (isEmpty(userPrincipalName))
                this.userPrincipalName = this.mail.substring(0, StringUtils.indexOf(this.mail, "@"));
            this.mailNickname = this.mail.substring(0, StringUtils.indexOf(this.mail, "@"));
        }

        // Nettoyage du userPrincipalName (si necessaire)
        if (isNotEmpty(this.userPrincipalName)
                && contains(this.userPrincipalName, "@")) {
            this.userPrincipalName = this.userPrincipalName.substring(0, StringUtils.indexOf(this.userPrincipalName, "@"));
        }

        // Ajout du userPrincipalName comme identifiant de connexion
        if (isNotEmpty(userPrincipalName)) {
            identities.add(new IdentityPayload(SignInType.PRINCIPALNAME.getValue(), tenant, this.userPrincipalName.concat("@").concat(tenant)));
            identities.add(new IdentityPayload(SignInType.FEDERATED.getValue(), tenant, this.userPrincipalName));
            if (!contains(this.mail, this.userPrincipalName))
                identities.add(new IdentityPayload(SignInType.USERNAME.getValue(), tenant, this.userPrincipalName));
        }
    }
}

