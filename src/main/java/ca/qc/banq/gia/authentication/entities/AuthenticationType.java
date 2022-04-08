package ca.qc.banq.gia.authentication.entities;

/**
 * Type d'authentification
 *
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @version 1.0
 * @since 13 sept. 2020
 */
public enum AuthenticationType {

    B2C("authentication.B2C"),
    AAD("authentication.AAD");

    /**
     * Valeur
     */
    private String value;

    AuthenticationType(String value) {
        this.value = value;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

}
