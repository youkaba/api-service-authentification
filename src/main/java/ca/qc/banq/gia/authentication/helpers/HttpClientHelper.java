// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package ca.qc.banq.gia.authentication.helpers;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-12
 */
@Slf4j
public class HttpClientHelper {

    public final static String ACCESS_TOKEN = "Authorization";
    public final static String AUTH_CODE = "authorization_code";
    public static final String EXPDATE_SESSION_NAME = "expdate";
    //public static final String IDTOKEN_SESSION_NAME = "idtoken";
    public static final String PRINCIPAL_SESSION_NAME = "principal";
    public static final String UID_SESSION_NAME = "unique_name";
    public final static String GET_USERINFOS_REST_URL = "get_user_infos";
    public final static String SIGNIN_URL = "signin_url";
    public final static String SIGNOUT_URL = "signout_url";
    public final static String BAnQ_CUSTOM_USERID = "extension_BAnQclientID";
    public final static String CLAIM_USERID = "oid";
    public final static String GRANT_TYPE_CREDENTIAL = "client_credentials";
    public final static String CLIENTID_PARAM = "appid";

    public final static String GIA_URLPATH_PARAM = "gia_url";
    public final static String GIA_CREATEUSER_ENDPOINT_PARAM = "createuser_endpoint";
    public final static String GIA_RESETPWD_ENDPOINT_PARAM = "useractivation_endpoint";

    public final static String FRONTOFFICE_APIURL = "/api/fo";
    public final static String SIGNIN_ENDPOINT = "/sign_in";
    public final static String SIGNOUT_ENDPOINT = "/sign_out";
    public final static String CREATEUSER_ENDPOINT = "/createUser";
    public final static String EDITUSER_ENDPOINT = "/editUser";
    public final static String RESETPWD_ENDPOINT = "/resetPassword";
    public final static String REDIRECTB2C_ENDPOINT = "/redirect2_b2c";
    public final static String REDIRECTAAD_ENDPOINT = "/redirect2_aad";

    public static final String FIND_APP_BYID_REQUEST_URL = "https://graph.microsoft.com/v1.0/servicePrincipals?$count=true&$filter=appId eq '$appId'&$select=id,displayName";
    public final static String ASSIGN_USERTOAPP_REQUEST_URL = "https://graph.microsoft.com/v1.0/servicePrincipals/$id/appRoleAssignments";

    public final static String EMAIL_REGEX = "^[\\w!#$%&’*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";

    public HttpClientHelper() {
    }

    public static String getResponseStringFromConn(HttpURLConnection conn) throws IOException {

        BufferedReader reader;
        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }

        return stringBuilder.toString();
    }

    public static JSONObject processResponse(int responseCode, String response) throws JSONException {

        JSONObject responseJson = new JSONObject();
        responseJson.put("responseCode", responseCode);

        if (response.equalsIgnoreCase("")) {
            responseJson.put("responseMsg", "");
        } else {
            responseJson.put("responseMsg", new JSONObject(response));
        }
        return responseJson;
    }


    /**
     * Execution d'un webservice REST
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> T callRestAPI(String url, HttpMethod method, Map<String, Object> queryParams, Class<T> returnType, Object body, HttpHeaders requestHeaders) {

        // Initialisation de la ressource bindee sur l'URL d'envoi
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);

        // Si la la map des parametres est non vide
        if (queryParams != null && !queryParams.isEmpty()) {
            for (Entry<String, Object> entry : queryParams.entrySet()) {
                builder.queryParam(entry.getKey(), entry.getValue());
            }
        }

        // Initialisation de l'objet RestTemplate
        RestTemplate restTemplate = new RestTemplate();
        if (requestHeaders == null) requestHeaders = getDefaultRequestHeaders();

        // objet contenant le corps et les param�tres entete
        HttpEntity<?> request = new HttpEntity(body, requestHeaders);

        // Logs
        log.warn("URI = " + builder.build().toUri());
        log.warn("REQUEST = " + request);

        // Envoi de la requete
        ResponseEntity<T> responseEntity = restTemplate.exchange(builder.build().toUri(), method, request, returnType);
        return responseEntity.getStatusCode().is2xxSuccessful() ? new ObjectMapper().convertValue(responseEntity.getBody(), returnType) : null;
    }

    private static HttpHeaders getDefaultRequestHeaders() {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        return requestHeaders;
    }
}
