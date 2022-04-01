/**
 *
 */
package ca.qc.banq.gia.authentication.exceptions;

/**
 * Exception GIA
 *
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-12
 */
public class GIAException extends RuntimeException {

    /**
     * Initialisation de l'exception a partir d'un message
     */
    public GIAException(String message) {
        super(message);
    }

    /**
     * Initialisation de l'exception a partir d'un throwable
     */
    public GIAException(Throwable ex) {
        super(ex);
    }

    /**
     * Initialisation de l'exception a partir d'un message et d'un throwable
     */
    public GIAException(String message, Throwable cause) {
        super(message, cause);
    }
}
