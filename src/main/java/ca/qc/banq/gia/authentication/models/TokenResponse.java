/**
 * 
 */
package ca.qc.banq.gia.authentication.models;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author francis.djiomou
 *
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("serial")
public class TokenResponse implements Serializable {

	String not_before;
	String token_type;
	String scope;
	String expires_in;
	String ext_expires_in;
	String access_token;
	String refresh_token;
	
}
