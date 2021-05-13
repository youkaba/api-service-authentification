/**
 * 
 */
package ca.qc.banq.gia.authentication.config;

import java.util.ArrayList;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SecurityConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


/**
 * Configuration de la documentation Swagger
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 13 sept. 2020
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

	String authLabel = "Authorization";
	
	@Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(getApiInfo())
                .enable(true)
                .securityContexts(Lists.newArrayList(securityContext()))
    			.securitySchemes(Lists.newArrayList(apiKey()));
    }
	
	
	private ApiInfo getApiInfo() {	
		 return new ApiInfoBuilder()
				.title("BAnQ Authentication Service - GIA")
				.description("Service de gestion des identites et des acces de BAnQ")
				.termsOfServiceUrl("https://www.banq.qc.ca/accueil/")
				.contact(new Contact("BAnQ Team","https://www.banq.qc.ca/outils/nous_joindre/index.html","boutique.banq@banq.qc.ca" ))
				.license("LGPL BAnQ Open Source")
				.licenseUrl("https://www.banq.qc.ca/outils/conditions_generales_dutilisation/")	
				.version("1.0")
				.build();
	}
	
	
	private ApiKey apiKey() {
		return new ApiKey(authLabel, authLabel, "header");
	}
	
	@Bean
	public SecurityConfiguration security() {
		return SecurityConfigurationBuilder.builder().clientId(null).clientSecret(null).realm(null).appName(null).scopeSeparator(",").additionalQueryStringParams(null).useBasicAuthenticationWithAccessCodeGrant(false).build();
	}
	
	
	private SecurityContext securityContext() {
		return SecurityContext.builder().securityReferences(defaultAuth()).forPaths(PathSelectors.any()).build();
	}
	 
	private ArrayList<SecurityReference> defaultAuth() {
		AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
		AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
		authorizationScopes[0] = authorizationScope;
		return Lists.newArrayList(new SecurityReference(authLabel, authorizationScopes));
	}
	
}
