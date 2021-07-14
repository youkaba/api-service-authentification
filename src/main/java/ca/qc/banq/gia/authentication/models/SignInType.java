/**
 * 
 */
package ca.qc.banq.gia.authentication.models;

/**
 * Type d'authentification
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 13 sept. 2020
 * @version 1.0
 */
public enum SignInType {

	PRINCIPALNAME("userPrincipalName"),
	USERNAME("userName"),
	EMAIL("emailAddress"),
	FEDERATED("federated");

	/**
	 * Valeur
	 */
	private String value;
	
	private SignInType(String value) {
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
