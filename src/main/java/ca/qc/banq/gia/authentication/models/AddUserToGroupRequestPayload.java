/**
 *
 */
package ca.qc.banq.gia.authentication.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author francis.djiomou
 *
 */
@Data
@NoArgsConstructor
public class AddUserToGroupRequestPayload implements Serializable {

    private String id;

    public AddUserToGroupRequestPayload(String userId) {
        this.id = "https://graph.microsoft.com/v1.0/directoryObjects/" + userId;
    }

    public String getJsonData() {
        return String.format("{\r\n \"@odata.id\": \"%s\"\r\n}", this.id);
    }
}
