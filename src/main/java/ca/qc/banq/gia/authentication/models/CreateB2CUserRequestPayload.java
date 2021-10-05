/**
 * 
 */
package ca.qc.banq.gia.authentication.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;

import ca.qc.banq.gia.authentication.helpers.HttpClientHelper;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author francis.djiomou
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("serial")
public class CreateB2CUserRequestPayload implements Serializable {

    @NotNull(message = "CreateB2CUserRequestPayload.userPrincipalName.NotNull")
    @ApiModelProperty(required = true, value = "userPrincipalName", example = "MelissaD@banq.qc.ca")
    private String userPrincipalName;
    
    @NotNull(message = "CreateB2CUserRequestPayload.userPrincipalName.NotNull")
    @ApiModelProperty(required = true, value = "userPrincipalName", example = "MelissaD@banq.qc.ca")
    private String password;

    @NotNull(message = "CreateB2CUserRequestPayload.displayName.NotNull")
    @ApiModelProperty(required = true, value = "displayName", example = "Melissa Darrow")
    private String displayName;
    
    @NotNull(message = "CreateB2CUserRequestPayload.mail.NotNull")
    @ApiModelProperty(value = "mail", example = "MelissaD@yahoo.com")
    private String mail;

    @ApiModelProperty(value = "mobilePhone", example = "+1 206 555 0110")
    private String mobilePhone;

    public UserRequestPayload toUserRequestPayload(String tenant) {
    	UserRequestPayload request = new UserRequestPayload();
    	request.setDisplayName(this.displayName);
    	request.setMail(this.mail);
    	request.setMobilePhone(this.mobilePhone);
    	request.setUserPrincipalName(this.userPrincipalName);
    	request.setAccountEnabled(true);
    	request.setPasswordProfile( new PasswordPolicy(this.password, false) );
    	List<IdentityPayload> identities = new ArrayList<IdentityPayload>();
    	boolean principalIsMail = this.userPrincipalName.equalsIgnoreCase(this.mail);
    	boolean principalIsAnEmail = this.userPrincipalName.matches(HttpClientHelper.EMAIL_REGEX);
    	if(principalIsAnEmail) {
    		if(!principalIsMail) {
    			identities.add(new IdentityPayload( SignInType.FEDERATED.getValue(), tenant, this.userPrincipalName ));
    		}
    		request.setUserPrincipalName(StringUtils.replace(this.userPrincipalName, "@", "."));
    	} else {
    		identities.add(new IdentityPayload( SignInType.USERNAME.getValue(), tenant, this.userPrincipalName ));
    	}
		identities.add(new IdentityPayload( SignInType.PRINCIPALNAME.getValue(), tenant, this.userPrincipalName.concat("@").concat(tenant) ));
    	identities.add(new IdentityPayload( SignInType.EMAIL.getValue(), tenant, this.mail ));
    	request.setIdentities(identities);
    	return request;
    }
}
