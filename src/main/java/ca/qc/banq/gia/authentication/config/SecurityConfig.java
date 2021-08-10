package ca.qc.banq.gia.authentication.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import ca.qc.banq.gia.authentication.helpers.HttpClientHelper;

/**
 * Configuration de securite de l'application
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-12
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Value("${spring.datasource.password}")
	String password;
	
	/*
	 * (non-javadoc)
	 * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#configure(org.springframework.security.config.annotation.web.builders.HttpSecurity)
	 */
    @Override
    protected void configure(HttpSecurity security) throws Exception {
        //Disable Spring's basic security settings as they are not relevant for this sample
        //security.httpBasic().disable();
        /* security.cors().and().csrf().disable().httpBasic().disable()
        .antMatcher("/**").authorizeRequests().antMatchers("/", "/h2-console**", "/error**", "/api/bo**").permitAll()
    	.and().authorizeRequests().anyRequest().permitAll()
    	.and().headers().frameOptions().sameOrigin()
    	.and().csrf().disable().headers().xssProtection(); */
        
        security
        .cors().and().csrf().disable()
		.authorizeRequests()
			.antMatchers("/error", "/api**", HttpClientHelper.FRONTOFFICE_APIURL.concat("**"), HttpClientHelper.SIGNIN_ENDPOINT, HttpClientHelper.SIGNOUT_ENDPOINT, HttpClientHelper.REDIRECTB2C_ENDPOINT, HttpClientHelper.REDIRECTAAD_ENDPOINT, HttpClientHelper.CREATEUSER_ENDPOINT, HttpClientHelper.RESETPWD_ENDPOINT)
			.permitAll()
			.and()
		.authorizeRequests()
			.antMatchers("/", "/apps**", "/doc", "/oups", "/env").authenticated()
			.and()
		.formLogin()
			//.loginPage("/login")
			.permitAll()
			.and()
		.logout().permitAll();
    }
    
    @Bean
	@Override
	public UserDetailsService userDetailsService() {
		UserDetails user =
			 User.withDefaultPasswordEncoder().username("admin").password(password)//.withDefaultPasswordEncoder()
				/*.username("user")
				.password("password") */
				.roles("USER")
				.build();

		return new InMemoryUserDetailsManager(user);
	}
    
}
