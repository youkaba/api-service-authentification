/**
 * 
 */
package ca.qc.banq.gia.authentication.models;

import java.io.Serializable;

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
public class IdentityPayload implements Serializable {

	private String signInType;
	private String issuer;
	private String issuerAssignedId;
}
