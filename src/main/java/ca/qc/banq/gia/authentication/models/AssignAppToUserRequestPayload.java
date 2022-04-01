/**
 *
 */
package ca.qc.banq.gia.authentication.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Payload de la requete de creation d'un nouvel utilisateur
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignAppToUserRequestPayload implements Serializable {

    String principalId;
    String resourceId;
    //String appRoleId = "00000000-0000-0000-0000-000000000000";

    public String getAppRoleId() {
        return "00000000-0000-0000-0000-000000000000";
    }
}
