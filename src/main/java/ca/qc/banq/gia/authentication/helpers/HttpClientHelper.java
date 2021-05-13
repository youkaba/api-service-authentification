// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package ca.qc.banq.gia.authentication.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author <a href="mailto:francis.djiomou@banq.qc.ca">Francis DJIOMOU</a>
 * @since 2021-05-12
 */
public class HttpClientHelper {

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
}
