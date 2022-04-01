/**
 *
 */
package ca.qc.banq.gia.authentication.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author francis.djiomou
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignAppToUserResponsePayload implements Serializable {

    String id;
    Date deletedDateTime;
    String appRoleId;
    Date createdDateTime;
    String principalDisplayName;
    String principalId;
    String principalType;
    String resourceDisplayName;
    String resourceId;
}
