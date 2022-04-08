package ca.qc.banq.gia.authentication.models;

import java.io.Serializable;

/**
 * @author francis.djiomou
 */
public record TokenResponse(
        String not_before,
        String token_type,
        String scope,
        String expires_in,
        String ext_expires_in,
        String access_token,
        String refresh_token) implements Serializable {
}
