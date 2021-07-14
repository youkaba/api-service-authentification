/**
 * 
 */
package ca.qc.banq.gia.authentication.models;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author francis.djiomou
 *
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@SuppressWarnings("serial")
public class GetTokenRequestPayload implements Serializable {

	@JsonProperty("grant_type")
	String grant_type; 
	@JsonProperty("client_secret")
	String client_secret;
	@JsonProperty("client_id")
	String client_id;
	String scope;
}
