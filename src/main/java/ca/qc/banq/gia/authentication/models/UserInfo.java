package ca.qc.banq.gia.authentication.models;

import java.io.Serializable;
import java.util.List;

/**
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-13
 */
public record UserInfo(
        //"@odata.context": "https://graph.microsoft.com/v1.0/$metadata#users/$entity",
        String id,
        List<String> businessPhones,
        String displayName,
        String givenName,
        String jobTitle,
        String mail,
        String mobilePhone,
        String officeLocation,
        String preferredLanguage,
        String surname,
        String userPrincipalName) implements Serializable {

}
