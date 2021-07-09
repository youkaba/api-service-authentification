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
	String grantType; 
	@JsonProperty("client_secret")
	String clientSecret;
	@JsonProperty("client_id")
	String clientId;
	String scope;
}
