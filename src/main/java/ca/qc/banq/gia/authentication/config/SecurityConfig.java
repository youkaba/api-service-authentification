package ca.qc.banq.gia.authentication.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * Configuration de securite de l'application
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-12
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	/*
	 * (non-javadoc)
	 * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#configure(org.springframework.security.config.annotation.web.builders.HttpSecurity)
	 */
    @Override
    protected void configure(HttpSecurity security) throws Exception {
        //Disable Spring's basic security settings as they are not relevant for this sample
        //security.httpBasic().disable();
        security.cors().and().csrf().disable().httpBasic().disable()
        .antMatcher("/**").authorizeRequests().antMatchers("/", "/h2-console**", "/error**", "/api/bo**").permitAll()
    	.and().authorizeRequests().anyRequest().permitAll()
    	.and().headers().frameOptions().sameOrigin()
    	.and().csrf().disable().headers().xssProtection();
    }
}
