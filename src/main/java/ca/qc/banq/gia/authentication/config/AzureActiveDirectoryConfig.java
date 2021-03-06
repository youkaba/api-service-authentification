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
@ConfigurationProperties("aad")
public class AzureActiveDirectoryConfig {

    private String tenantId;
    private String authority;
    private String redirectUriGraph;
    private String msGraphEndpointHost;
    private String msGraphScope;
    private String accessGraphTokenUri;
    private String msGraphUsersEndpoint;
    private String msGraphAddUserToGroupEndpoint;
    private String scope;
}
