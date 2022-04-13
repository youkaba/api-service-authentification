package ca.qc.banq.gia.authentication.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Exception GIA
 *
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-12
 */
@Getter
public class GIAException extends RuntimeException {
    private final HttpStatus httpStatus;

    public GIAException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

}
