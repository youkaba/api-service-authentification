package ca.qc.banq.gia.authentication.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author francis.djiomou
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TokenResponse implements Serializable {

    private String not_before;
    private String token_type;
    private String scope;
    private String expires_in;
    private String ext_expires_in;
    private String access_token;
    private String refresh_token;

}
