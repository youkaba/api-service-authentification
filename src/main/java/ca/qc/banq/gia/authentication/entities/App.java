/**
 * 
 */
package ca.qc.banq.gia.authentication.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Entite App : Representant une Application de BAnQ
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-12
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "gia_app")
@SuppressWarnings("serial")
public class App implements Serializable {

	@Id
	@Column
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	/** Nom de l'application */
	@Column(name = "title")
	private String title;
	
	/** Type d'authentification */
	@Enumerated(EnumType.STRING)
	@Column(name = "type_auth")
	@NotNull(message = "App.typeAuth.NotNull")
	private TypeAuth typeAuth;

	/** Page d'accueil de l'application */
	@Column(name = "home_url")
	@NotNull(message = "App.homeUrl.NotNull")
	private String homeUrl;

	/** Client Id de l'application sur la plateforme Azure */
	@Column(name = "client_id")
	@NotNull(message = "App.clientId.NotNull")
	private String clientId;

	/** Valeur du certificat sur la plateforme Azure */
	@Column(name = "cert_secret")
	@NotNull(message = "App.certSecretValue.NotNull")
	private String certSecretValue;
	
	/** Etendue de l'application sur la plateforme Azure */
	public String getApiScope() {
		return this.typeAuth.equals(TypeAuth.B2C) ? this.clientId : null;
	}
	
	/** MAJ de l'application */
	@JsonIgnore
	public void update(App app) {
		this.certSecretValue = app.getCertSecretValue();
		this.clientId = app.getClientId();
		this.homeUrl = app.getHomeUrl();
		this.title = app.getTitle();
		this.typeAuth = app.getTypeAuth();
	}
}
