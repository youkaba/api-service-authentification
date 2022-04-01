package ca.qc.banq.gia.authentication.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author francis.djiomou
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetIdentitiesResponse implements Serializable {

    List<IdentityPayload> value;
}
