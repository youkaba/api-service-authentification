/**
 * 
 */
package ca.qc.banq.gia.authentication.models;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload de la requete de creation d'un nouvel utilisateur
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("serial")
public class CreateUserRequestPayload implements Serializable {

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
    
    @NotNull(message = "CreateUserRequestPayload.mailNickname.NotNull")
    @ApiModelProperty(value = "mailNickname", example = "MelissaD")
    private String mailNickname;
    
    @NotNull(message = "CreateUserRequestPayload.passwordPolicies.NotNull")
    @ApiModelProperty(value = "passwordPolicies", example = "\"passwordProfile\": {\"password\": \"87f312a1-38ec-1179-c230-bbd2ab1d0d9c\",\"forceChangePasswordNextSignIn\": false}")
    private PasswordPolicy passwordPolicies;
    
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
    
    @ApiModelProperty(value = "mobilePhone", example = "+1 206 555 0110")
    private String mobilePhone;
    
    @ApiModelProperty(value = "usageLocation", example = "US")
    private String usageLocation;
    
    @NotNull(message = "CreateUserRequestPayload.userPrincipalName.NotNull")
    @ApiModelProperty(required = true, value = "userPrincipalName", example = "MelissaD@banq.qc.ca")
    private String userPrincipalName;
	
}

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("serial")
class PasswordPolicy implements Serializable {
	
	@ApiModelProperty(required = true, value = "password", example = "UEA1NXcwckQ=")
	private String password;
	
	@ApiModelProperty(required = true, value = "forceChangePasswordNextSignIn", example = "false")
	private Boolean forceChangePasswordNextSignIn = false;
}
