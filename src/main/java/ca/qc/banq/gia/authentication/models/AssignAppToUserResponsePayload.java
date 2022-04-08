/**
 *
 */
package ca.qc.banq.gia.authentication.models;

import java.util.Date;

/**
 * @author francis.djiomou
 */
public record AssignAppToUserResponsePayload(
        String id,
        Date deletedDateTime,
        String appRoleId,
        Date createdDateTime,
        String principalDisplayName,
        String principalId,
        String principalType,
        String resourceDisplayName,
        String resourceId) {


}