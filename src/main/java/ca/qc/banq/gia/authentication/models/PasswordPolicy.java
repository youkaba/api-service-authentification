/**
 * 
 */
package ca.qc.banq.gia.authentication.models;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
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
public class PasswordPolicy implements Serializable {
	
	@ApiModelProperty(required = true, value = "password", example = "UEA1NXcwckQ=")
	private String password;
	
	@ApiModelProperty(required = true, value = "forceChangePasswordNextSignIn", example = "false")
	private Boolean forceChangePasswordNextSignIn = false;
}