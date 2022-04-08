/**
 *
 */
package ca.qc.banq.gia.authentication.models;

/**
 * @author francis.djiomou
 */

public record AddUserToGroupRequestPayload(String id) {

    public String getJsonData() {
        return String.format("{\r\n \"@odata.id\": \"%s\"\r\n}", this.id);
    }
}

