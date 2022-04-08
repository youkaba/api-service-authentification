package ca.qc.banq.gia.authentication.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration AAD
 *
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-10
 */
//@Data
@Configuration
//@ConfigurationProperties("gia")
public class GIAConfig {

    private String clientId;
    private String clientSecret;
    private String redirectUri;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
