package ca.qc.banq.gia.authentication.models;

import java.util.Date;

/**
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-12
 */
public record StateData(String nonce, Date expirationDate) {
}