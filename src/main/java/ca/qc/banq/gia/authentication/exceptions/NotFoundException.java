package ca.qc.banq.gia.authentication.exceptions;

import static org.springframework.http.HttpStatus.NOT_FOUND;

public class NotFoundException extends GIAException {

    public NotFoundException(String message) {
        super(NOT_FOUND, message);
    }
}
