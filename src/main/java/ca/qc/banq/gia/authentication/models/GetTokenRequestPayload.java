/**
 *
 */
package ca.qc.banq.gia.authentication.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

/**
 * @author francis.djiomou
 *
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class GetTokenRequestPayload implements Serializable {

    @JsonProperty("grant_type")
    private String grant_type;
    @JsonProperty("client_secret")
    private String client_secret;
    @JsonProperty("client_id")
    private String client_id;
    private String scope;
}
