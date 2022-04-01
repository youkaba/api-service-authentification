/**
 *
 */
package ca.qc.banq.gia.authentication.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Payload de la requete de creation d'un nouvel utilisateur
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FindAppByNameResponsePayload implements Serializable {
    private List<AppData> value;
}
