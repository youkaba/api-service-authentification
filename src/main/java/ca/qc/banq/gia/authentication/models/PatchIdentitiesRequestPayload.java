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
 * @author francis.djiomou
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("serial")
public class PatchIdentitiesRequestPayload implements Serializable {

	List<IdentityPayload> value;
}
