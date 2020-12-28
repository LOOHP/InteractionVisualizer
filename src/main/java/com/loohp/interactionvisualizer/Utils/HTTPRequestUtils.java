package com.loohp.interactionvisualizer.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class HTTPRequestUtils {
	
	public static JSONObject getJSONResponse(String link) throws Exception {    	
		URL url = new URL(link);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setUseCaches(false);
        connection.setDefaultUseCaches(false);
        connection.addRequestProperty("User-Agent", "Mozilla/5.0");
        connection.addRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
        connection.addRequestProperty("Pragma", "no-cache");
        if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
        	String reply = new BufferedReader(new InputStreamReader(connection.getInputStream())).lines().collect(Collectors.joining());
            return (JSONObject) new JSONParser().parse(reply);
        }
        throw new IOException("HTTP error code returned.");
	}

}
