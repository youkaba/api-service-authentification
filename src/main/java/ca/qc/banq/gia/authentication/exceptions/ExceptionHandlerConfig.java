/**
 * 
 */
package ca.qc.banq.gia.authentication.exceptions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import ca.qc.banq.gia.authentication.config.TranslatorConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * Intercepteur Generique des exceptions levees dans l'application
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-12
 */
@Slf4j
@ControllerAdvice
public class ExceptionHandlerConfig extends ResponseEntityExceptionHandler  {
	
	/** Injection du composant multilingue */
	@Autowired
	TranslatorConfig translator;
	
	/**
	 * interception de tout autre type d'exception
	 * @param ex
	 * @param request
	 * @return
	 */
	@ExceptionHandler(value = { Exception.class, GIAException.class, ConstraintViolationException.class })
	protected ResponseEntity<List<String>> handleConflict(Exception ex, WebRequest request) {
		
		// Calcul du statut et du message d'erreur a retourner
		HttpStatus status = (ex instanceof GIAException ) ? HttpStatus.BAD_REQUEST : HttpStatus.INTERNAL_SERVER_ERROR ;

		// initialisation de la liste des erreurs a retourner
		List<String> erreurs = new ArrayList<String>();
		if(status.equals(HttpStatus.INTERNAL_SERVER_ERROR)) erreurs.add(translator.translate("server.error.msg"));
		
        // Verification s'il s'agit d'une exception liee a une violation de contrainte d'integrite
		if(ExceptionUtils.getRootCause(ex) instanceof ConstraintViolationException) {
			ConstraintViolationException ex2 = (ConstraintViolationException) ExceptionUtils.getRootCause(ex);
			// recuperation de la liste des contraintes violees
			erreurs.addAll( ex2.getConstraintViolations().stream().map(x -> translator.translate(x.getMessage())).collect(Collectors.toList()) );
			
			// MAJ du type et du message d'erreur a retourner
			status = HttpStatus.BAD_REQUEST;
			erreurs.add( ex.getCause().getMessage() );
			
		} else erreurs.add(translator.translate(ex.getMessage()));
		
		// Log
        log.error(ex.getMessage(), ex);
        
        // Return Response
        return new ResponseEntity<List<String>>(erreurs, status);
	}
	
	/**
	 * Interception des exceptions de validation des donnees
	 */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException excetion, HttpHeaders headers, HttpStatus statut, WebRequest request) {
        List<String> erreurs = excetion.getBindingResult().getFieldErrors().stream().map(x -> translator.translate(x.getDefaultMessage()) ).collect(Collectors.toList());
        return ResponseEntity.badRequest().body(erreurs);
    }		
	
}
