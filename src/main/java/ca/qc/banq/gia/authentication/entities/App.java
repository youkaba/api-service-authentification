/**
 * 
 */
package ca.qc.banq.gia.authentication.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ca.qc.banq.gia.authentication.models.AppPayload;
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

	/** Client Id de l'application sur la plateforme Azure */
	@Id
	@Column(name = "client_id")
	@NotNull(message = "App.clientId.NotNull")
	@NotEmpty(message = "App.clientId.NotNull")
	private String clientId;
	
	/** Nom de l'application */
	@Column(name = "title")
	@NotNull(message = "App.title.NotNull")
	@NotEmpty(message = "App.title.NotNull")
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

	/** Valeur du certificat sur la plateforme Azure */
	@Column(name = "cert_secret")
	@NotNull(message = "App.certSecretValue.NotNull")
	private String certSecretValue;
	
	/** Flux utilisateur pour la connexion */
	@Column(name = "flux_signin")
	private String policySignUpSignIn;

	/** Flux utilisateur pour la reinitialisation de mot de passe */
	@Column(name = "flux_reset_pwd")
	private String policyResetPassword;

	/** Flux utilisateur pour la modification de profil utilisateur */
	@Column(name = "flux_edit_profile")
	private String policyEditProfile;

	/** Groupe des utilisateurs de l'application */
	@Column(name = "users_group_id")
	private String usersGroupId;
	
	/** MAJ de l'application */
	@JsonIgnore
	public void update(App app) {
		this.certSecretValue = app.getCertSecretValue();
		this.clientId = app.getClientId();
		this.homeUrl = app.getHomeUrl();
		this.title = app.getTitle();
		this.typeAuth = app.getTypeAuth();
		this.policySignUpSignIn = app.getPolicySignUpSignIn();
		this.policyResetPassword = app.getPolicyResetPassword();
		this.policyEditProfile = app.getPolicyEditProfile();
		this.usersGroupId = app.getUsersGroupId();
	}
	
	public AppPayload toDTO(String loginUrl, String redirectApp, String loginOut) {
		return new AppPayload(this.clientId, this.title, this.typeAuth, this.homeUrl, this.certSecretValue, this.typeAuth.equals(TypeAuth.B2C) ? this.clientId : "", loginUrl, loginOut, this.policySignUpSignIn, this.policyResetPassword, this.policyEditProfile, this.usersGroupId, redirectApp);
	}
	
	@Transient
	private boolean nouveau = true;
}
