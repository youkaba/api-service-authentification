/**
 * 
 */
package ca.qc.banq.gia.authentication.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.qc.banq.gia.authentication.entities.App;
import ca.qc.banq.gia.authentication.entities.TypeAuth;
import ca.qc.banq.gia.authentication.servicesmetier.GiaBackOfficeService;

/**
 * Filter de requetes de l'application
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-12
 */
@Component
public class AuthFilter implements Filter {

	/** Identifiant du parametre identifiant des applications */
	public final static String APP_ID = "appid";

	@Autowired
	AuthFilterB2C filterB2C;
	
	@Autowired
	AuthFilterAAD filterAAD;
	
	@Autowired
	GiaBackOfficeService giaBOService;
	
	/** Liste des URIs a filtrer */
	List<String> excludedUrls = Arrays.asList("/", "/b2c", "/aad");
	
	/*
	 * (non-javadoc)
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		
		// On recherche l'id de l'application dans les attributs de la requete
		String appId = request.getParameter(APP_ID) != null ? request.getParameter(APP_ID).toString() : null;
		App app = appId != null ? giaBOService.findById(Long.valueOf(appId)) : null;
		
		// Si aucune application na ete fournie dans l'url,
		if(app == null) {
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			if(excludedUrls.contains(httpRequest.getServletPath())) {
				//  on affiche la page d'erreur
				if( filterAAD.getAuthHelper().getApp() == null && filterB2C.getAuthHelper().getApp() == null ) {
					HttpServletResponse httpResponse = (HttpServletResponse) response;
					httpResponse.setStatus(500);
		            request.setAttribute("error", "unable to find attribute appid");
		            request.getRequestDispatcher("/error").forward(request, response);
		            return;
				} else {
					if(filterAAD.getAuthHelper().getApp() != null) {
						// On execute le filtre de request pour l'authentification AAD
						filterAAD.doFilter(request, response, chain);
						return;
					} else {
						// On execute le filtre de request pour l'authentification B2C
						filterB2C.doFilter(request, response, chain);
						return;
					}
				}
			}
			chain.doFilter(request, response);
			return;
		}
		
		// Si l'application sollicitee fournit un type d'authentification B2C
		if(app.getTypeAuth().equals(TypeAuth.B2C)) {
			// On execute le filtre de request pour l'authentification B2C
			filterB2C.getAuthHelper().init(app);
			filterB2C.doFilter(request, response, chain);
		} else {
			// On execute le filtre de request pour l'authentification AAD
			filterAAD.getAuthHelper().init(app);
			filterAAD.doFilter(request, response, chain);
		}
	}

}
