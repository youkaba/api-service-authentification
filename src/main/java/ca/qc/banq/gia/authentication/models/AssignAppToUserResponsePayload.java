/**
 * 
 */
package ca.qc.banq.gia.authentication.models;

import java.io.Serializable;
import java.util.Date;

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
