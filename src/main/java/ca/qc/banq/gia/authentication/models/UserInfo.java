/**
 * 
 */
package ca.qc.banq.gia.authentication.models;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("serial")
public class UserInfo implements Serializable {

	//"@odata.context": "https://graph.microsoft.com/v1.0/$metadata#users/$entity",
	private List<String> businessPhones;
	private String displayName;
	private String givenName;
	private String jobTitle;
	private String mail;
	private String mobilePhone;
	private String officeLocation;
	private String preferredLanguage;
	private String surname;
	private String userPrincipalName;
	private String id;
	
}
