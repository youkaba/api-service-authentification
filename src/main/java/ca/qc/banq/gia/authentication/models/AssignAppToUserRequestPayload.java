package ca.qc.banq.gia.authentication.models;

/**
 * Payload de la requete de creation d'un nouvel utilisateur
 *
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-13
 */

public record AssignAppToUserRequestPayload(String principalId, String resourceId) {

    //String appRoleId = "00000000-0000-0000-0000-000000000000";

    public String getAppRoleId() {
        return "00000000-0000-0000-0000-000000000000";
    }
}
