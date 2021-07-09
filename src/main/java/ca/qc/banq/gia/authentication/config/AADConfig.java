/**
 * 
 */
package ca.qc.banq.gia.authentication.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * Configuration AAD
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-10
 */
@Data
@Component
@ConfigurationProperties("aad")
public class AADConfig {

	private String tenantId;
	private String authority;
	private String redirectUriGraph;
	private String msGraphEndpointHost;
	private String msGraphScope;
	private String accessGraphTokenUri;
	private String msGraphUsersEndpoint;
}
