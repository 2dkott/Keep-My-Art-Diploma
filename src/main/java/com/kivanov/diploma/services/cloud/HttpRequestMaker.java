package com.kivanov.diploma.services.cloud;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.FileBody;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.*;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;

@Slf4j
public class HttpRequestMaker {

    private final String GET = "GET";
    private final String PUT = "PUT";

    public String getGetResponseBody(String urlString, String oauthToken) throws IOException {
        return sendRequest(GET, urlString, oauthToken);
    }

    public String getPutResponseBody(String urlString, String oauthToken) throws IOException {
        return sendRequest(PUT, urlString, oauthToken);
    }

    private String sendRequest(String method, String urlString, String oauthToken) throws IOException {
        log.info("Set up request to Cloud with url {}", urlString);
        URL url= new URL(urlString);
        URLConnection urlConnection = url.openConnection();
        HttpsURLConnection httpsConnection = (HttpsURLConnection) urlConnection;
        httpsConnection.setRequestMethod(method);
        httpsConnection.setRequestProperty("Authorization", "OAuth " + oauthToken);
        httpsConnection.connect();
        log.info("Response Code is {}", httpsConnection.getResponseCode());
        if (httpsConnection.getResponseCode()==200 || httpsConnection.getResponseCode()==201) {
            log.info("Response: {}", httpsConnection.getContent().toString());
            BufferedReader responseStream = new BufferedReader(new InputStreamReader(httpsConnection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = responseStream.readLine()) != null) {
                response.append(inputLine);
            }
            responseStream.close();
            return response.toString();
        }

        if (httpsConnection.getResponseCode()==409) {
            log.info("Ресурс {} уже существует", urlString);
        }
        if (httpsConnection.getResponseCode()==404) {
            log.info("Не удалось найти запрошенный ресурс {}", urlString);
        }
        return "";
    }

    public void createDirectory(String urlString, String oauthToken) throws IOException {
        log.info("Set up request to create Directory in Cloud with url {}", urlString);
        String response = getPutResponseBody(urlString, oauthToken);
        log.info("Response {}", response);

    }

    public void sendFile(String urlString, String oauthToken, File file) throws IOException {
        log.info("Set up request to upload Cloud with url {}", urlString);
        String uploadUrl = JsonMapper.mapJsonToUploadUrl(getGetResponseBody(urlString, oauthToken));
        HttpEntity entity = MultipartEntityBuilder.create()
                .addPart("file", new FileBody(file))
                .build();
        HttpPost request = new HttpPost(uploadUrl);
        request.setEntity(entity);

        HttpClient client = HttpClientBuilder.create().build();
        HttpResponse response = client.execute(request);
        log.info("Response {}", response);
    }

    public void getFile(String urlString, String oauthToken, File file) throws IOException {
        log.info("Set up request to upload Cloud with url {}", urlString);
        String downLoadUrl = JsonMapper.mapJsonToUploadUrl(getGetResponseBody(urlString, oauthToken));
        CloseableHttpClient client = HttpClients.createDefault();
        try (CloseableHttpResponse response = client.execute(new HttpGet(downLoadUrl))) {
            log.info("Response {}", response);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                try (FileOutputStream outstream = new FileOutputStream(file)) {
                    entity.writeTo(outstream);
                }
            }
        }
    }
}
