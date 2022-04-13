package ca.qc.banq.gia.authentication.exceptions;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class InvalidException extends GIAException {

    /**
     * Initialisation de l'exception a partir d'un message et d'un throwable
     *
     * @param message the error message
     */
    public InvalidException(String message) {
        super(BAD_REQUEST, message);
    }
}
