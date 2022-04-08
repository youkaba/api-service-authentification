/**
 *
 */
package ca.qc.banq.gia.authentication.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * Entite App : Representant une Application de BAnQ
 *
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-12
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "gia_app")
public class App implements Serializable {

    /**
     * Client Id de l'application sur la plateforme Azure
     */
    @Id
    @Column(name = "client_id")
    @NotNull(message = "app.clientId.NotNull")
    @NotEmpty(message = "app.clientId.NotNull")
    private String clientId;

    /**
     * Nom de l'application
     */
    @Column(name = "title")
    @NotNull(message = "app.title.NotNull")
    @NotEmpty(message = "app.title.NotNull")
    private String title;

    /**
     * Type d'authentification
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type_auth")
    @NotNull(message = "app.authenticationType.NotNull")
    private AuthenticationType authenticationType;

    /**
     * Page d'accueil de l'application
     */
    @Column(name = "home_url")
    @NotNull(message = "app.homeUrl.NotNull")
    private String homeUrl;

    /**
     * Valeur du certificat sur la plateforme Azure
     */
    @Column(name = "cert_secret")
    @NotNull(message = "app.certSecretValue.NotNull")
    private String certSecretValue;

    /**
     * Flux utilisateur pour la connexion
     */
    @Column(name = "flux_signin")
    private String policySignUpSignIn;

    /**
     * Flux utilisateur pour la reinitialisation de mot de passe
     */
    @Column(name = "flux_reset_pwd")
    private String policyResetPassword;

    /**
     * Flux utilisateur pour la modification de profil utilisateur
     */
    @Column(name = "flux_edit_profile")
    private String policyEditProfile;

    /**
     * Groupe des utilisateurs de l'application
     */
    @Column(name = "users_group_id")
    private String usersGroupId;
    @Transient
    private boolean nouveau;

    /**
     * MAJ de l'application
     */
    @JsonIgnore
    public void update(App app) {
        this.certSecretValue = app.getCertSecretValue();
        this.clientId = app.getClientId();
        this.homeUrl = app.getHomeUrl();
        this.title = app.getTitle();
        this.authenticationType = app.getAuthenticationType();
        this.policySignUpSignIn = app.getPolicySignUpSignIn();
        this.policyResetPassword = app.getPolicyResetPassword();
        this.policyEditProfile = app.getPolicyEditProfile();
        this.usersGroupId = app.getUsersGroupId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        App app = (App) o;
        return nouveau == app.nouveau
                && clientId.equals(app.clientId)
                && title.equals(app.title)
                && authenticationType == app.authenticationType
                && homeUrl.equals(app.homeUrl)
                && certSecretValue.equals(app.certSecretValue)
                && Objects.equals(policySignUpSignIn, app.policySignUpSignIn)
                && Objects.equals(policyResetPassword, app.policyResetPassword)
                && Objects.equals(policyEditProfile, app.policyEditProfile)
                && Objects.equals(usersGroupId, app.usersGroupId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, title, authenticationType, homeUrl, certSecretValue, policySignUpSignIn, policyResetPassword, policyEditProfile, usersGroupId, nouveau);
    }
}
