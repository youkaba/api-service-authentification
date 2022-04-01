/**
 *
 */
package ca.qc.banq.gia.authentication.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author francis.djiomou
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdentityPayload implements Serializable {

    private String signInType;
    private String issuer;
    private String issuerAssignedId;
}
