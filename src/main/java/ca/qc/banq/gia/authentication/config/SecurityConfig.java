package ca.qc.banq.gia.authentication.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import com.azure.spring.aad.webapp.AADWebSecurityConfigurerAdapter;

import ca.qc.banq.gia.authentication.helpers.HttpClientHelper;

/**
 * Configuration de securite de l'application
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-12
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends AADWebSecurityConfigurerAdapter { // WebSecurityConfigurerAdapter {
	
	/*
	 * (non-javadoc)
	 * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#configure(org.springframework.security.config.annotation.web.builders.HttpSecurity)
	 */
    @Override
    protected void configure(HttpSecurity security) throws Exception {
        security
        .cors().and().csrf().disable()
		.authorizeRequests()
			.antMatchers("/error", "/api**", HttpClientHelper.FRONTOFFICE_APIURL.concat("**"), HttpClientHelper.SIGNIN_ENDPOINT, HttpClientHelper.SIGNOUT_ENDPOINT, HttpClientHelper.REDIRECTB2C_ENDPOINT, HttpClientHelper.REDIRECTAAD_ENDPOINT, HttpClientHelper.CREATEUSER_ENDPOINT, HttpClientHelper.RESETPWD_ENDPOINT)
			.permitAll()
			.and()
		.authorizeRequests()
			.antMatchers( "/", "/apps**", "/doc", "/oups", "/env", "/signout").authenticated()
			.and().oauth2Login()
			;//.and().logout().permitAll();
			
    }
    
}
