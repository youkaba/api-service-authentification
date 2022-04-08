package ca.qc.banq.gia.authentication.config;

import com.azure.spring.aad.webapp.AADWebSecurityConfigurerAdapter;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import static ca.qc.banq.gia.authentication.helpers.HttpClientHelper.*;


/**
 * Configuration de securite de l'application
 *
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-12
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends AADWebSecurityConfigurerAdapter { // WebSecurityConfigurerAdapter {

    private static final String[] AUTH_LIST = {
            "/error", "/api**", "/h2-console/**", FRONTOFFICE_APIURL.concat("**"),
            SIGNIN_ENDPOINT, SIGNOUT_ENDPOINT,
            REDIRECTB2C_ENDPOINT, REDIRECTAAD_ENDPOINT,
            CREATEUSER_ENDPOINT, RESETPWD_ENDPOINT
    };

    @Override
    protected void configure(HttpSecurity security) throws Exception {
        security
                .cors().and().csrf().disable()
                .authorizeRequests()
                .antMatchers(AUTH_LIST)
                .permitAll()
                .and()
                .authorizeRequests()
                .antMatchers("/", "/apps**", "/doc", "/oups", "/env", "/signout").authenticated()
                .and().oauth2Login()
                .and()
                .headers().frameOptions().sameOrigin();

//        security.headers().frameOptions().disable();
        //.and().logout().permitAll();

    }

}
