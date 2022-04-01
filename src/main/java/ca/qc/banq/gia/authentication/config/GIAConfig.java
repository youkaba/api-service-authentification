/**
 *
 */
package ca.qc.banq.gia.authentication.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration AAD
 *
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-10
 */
@Data
@Component
@ConfigurationProperties("gia")
public class GIAConfig {

    private String clientId;
    private String clientSecret;
    private String redirectUri;
}
