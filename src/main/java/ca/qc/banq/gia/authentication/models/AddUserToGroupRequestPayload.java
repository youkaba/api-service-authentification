/**
 * 
 */
package ca.qc.banq.gia.authentication.models;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author francis.djiomou
 *
 */
@Data
@NoArgsConstructor
@SuppressWarnings("serial")
public class AddUserToGroupRequestPayload implements Serializable {

	String id;

	public AddUserToGroupRequestPayload(String userId) {
		this.id = "https://graph.microsoft.com/v1.0/directoryObjects/" +  userId;
	}
	
}
