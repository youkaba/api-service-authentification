// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package ca.qc.banq.gia.authentication.helpers;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-12
 */
@Slf4j
public class HttpClientHelper {
	
	public final static String ACCESS_TOKEN = "Authorization";
	public static final String EXPDATE_SESSION_NAME = "expdate";
	public static final String IDTOKEN_SESSION_NAME = "idtoken";
	public static final String PRINCIPAL_SESSION_NAME = "principal";

	public  HttpClientHelper() {}

    public static String getResponseStringFromConn(HttpURLConnection conn) throws IOException {

        BufferedReader reader;
        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }
        StringBuilder stringBuilder= new StringBuilder();
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
	 * @param <T>
	 * @param url
	 * @param method
	 * @param queryParams
	 * @param returnType
	 * @param body
	 * @param requestHeaders
	 * @return
	 */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> T callRestAPI(String url, HttpMethod method, Map<String, Object> queryParams, Class<T> returnType, Object body, HttpHeaders requestHeaders) {

		// Initialisation de la ressource bindee sur l'URL d'envoi
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl( url );
		
		// Si la la map des parametres est non vide
		if(queryParams != null && !queryParams.isEmpty()) {
			for(Entry<String, Object> entry : queryParams.entrySet()) {
				builder.queryParam(entry.getKey(), entry.getValue());
			}
		}
		
		// Initialisation de l'objet RestTemplate
		RestTemplate restTemplate = new RestTemplate();
		if(requestHeaders == null) requestHeaders = getDefaultRequestHeaders();
		//if(AUTH_TOKEN != null) requestHeaders.add("Authorization", AUTH_TOKEN);
		
	  	// objet contenant le corps et les param�tres entete
	  	HttpEntity<?> request = new HttpEntity(body, requestHeaders);

	  	// Logs
	  	log.warn("URI = " + builder.build().toUri());
	  	log.warn("REQUEST = " + request);
	  	
		// Envoi de la requete
	  	ResponseEntity<T> responseEntity = restTemplate.exchange(builder.build().toUri(), method, request, returnType );
	  	return responseEntity.getStatusCode().is2xxSuccessful() ? new ObjectMapper().convertValue(responseEntity.getBody(), returnType) : null;
    }
    
    private static HttpHeaders getDefaultRequestHeaders() {
	  	HttpHeaders requestHeaders = new HttpHeaders();
	  	requestHeaders.setContentType(MediaType.APPLICATION_JSON);
	  	return requestHeaders;
    }
    
}
