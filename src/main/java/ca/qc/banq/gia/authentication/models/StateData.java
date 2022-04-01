package ca.qc.banq.gia.authentication.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StateData implements Serializable {
    private String nonce;
    private Date expirationDate;

}