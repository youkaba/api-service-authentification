/**
 *
 */
package ca.qc.banq.gia.authentication.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * @author francis.djiomou
 */

public record GetTokenRequestPayload(
        @JsonProperty("grant_type")
        String grant_type,
        @JsonProperty("client_secret")
        String client_secret,
        @JsonProperty("client_id")
        String client_id,
        String scope
) implements Serializable {
}
