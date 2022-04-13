package ca.qc.banq.gia.authentication.exceptions;

import static org.springframework.http.HttpStatus.EXPECTATION_FAILED;

public class ClientErrorException extends GIAException {

    /**
     * Initialisation de l'exception a partir d'un message et d'un throwable
     *
     * @param message the error message
     */
    public ClientErrorException(String message) {
        super(EXPECTATION_FAILED, message);
    }
}
