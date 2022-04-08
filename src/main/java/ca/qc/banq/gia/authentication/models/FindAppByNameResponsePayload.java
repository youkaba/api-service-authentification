/**
 *
 */
package ca.qc.banq.gia.authentication.models;

import java.util.List;

/**
 * Payload de la requete de creation d'un nouvel utilisateur
 *
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-13
 */
public record FindAppByNameResponsePayload(List<AppData> value) {
}
