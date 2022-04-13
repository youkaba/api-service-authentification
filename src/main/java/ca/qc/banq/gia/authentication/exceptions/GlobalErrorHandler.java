package ca.qc.banq.gia.authentication.exceptions;

import ca.qc.banq.gia.authentication.config.TranslatorConfig;
import ca.qc.banq.gia.authentication.models.ErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Optional;

/**
 * Intercepteur Generique des exceptions levees dans l'application
 *
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-12
 */
@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalErrorHandler {

    /**
     * Injection du composant multilingue
     */
    private final TranslatorConfig translator;


    @ExceptionHandler(GIAException.class)
    public ResponseEntity<ErrorResponse> handleGIAException(GIAException e) {
        return buildErrorResponse(e.getHttpStatus(), e.getMessage(), e);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR, "server.error.msg", e);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        String message = Optional.ofNullable(e.getFieldError())
                .map(fieldError -> String.format("The %s %s",
                        fieldError.getField(), fieldError.getDefaultMessage()))
                .orElse(e.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, e);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus httpStatus, String message, Exception e) {
        log.error("Failed to process request - HTTP {} - {}", httpStatus.value(), message, e);
        return ResponseEntity
                .status(httpStatus)
                .body(new ErrorResponse(message));
    }
}