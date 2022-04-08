package ca.qc.banq.gia.authentication.models;

import java.io.Serializable;
import java.util.List;

/**
 * @author francis.djiomou
 */

public record GetIdentitiesResponse(List<IdentityPayload> value) implements Serializable {
}
