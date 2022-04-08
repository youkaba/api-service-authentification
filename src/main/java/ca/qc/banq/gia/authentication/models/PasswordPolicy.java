/**
 *
 */
package ca.qc.banq.gia.authentication.models;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author francis.djiomou
 */
public record PasswordPolicy(
        @ApiModelProperty(required = true, value = "password", example = "UEA1NXcwckQ=")
        String password,

        @ApiModelProperty(required = true, value = "forceChangePasswordNextSignIn", example = "false")
        Boolean forceChangePasswordNextSignIn) {

    public PasswordPolicy(String password) {
        this(password, false);
    }
}