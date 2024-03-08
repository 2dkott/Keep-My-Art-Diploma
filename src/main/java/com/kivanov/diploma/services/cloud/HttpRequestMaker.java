package com.kivanov.diploma.services.cloud;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class HttpRequestMaker {

    public String getResponse(String urlString, String oauthToken) throws IOException {
        URL url= new URL(urlString);
        URLConnection urlConnection = url.openConnection();
        HttpsURLConnection httpsConnection = (HttpsURLConnection) urlConnection;
        httpsConnection.setRequestMethod("GET");
        httpsConnection.setRequestProperty("Authorization", "OAuth " + oauthToken);
        httpsConnection.connect();
        BufferedReader responseStream = new BufferedReader(new InputStreamReader(httpsConnection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = responseStream.readLine()) != null) {
            response.append(inputLine);
        }
        responseStream.close();
        return response.toString();
    }
}
