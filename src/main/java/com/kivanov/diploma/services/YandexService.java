package com.kivanov.diploma.services;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;
import com.kivanov.diploma.model.WebUrls;
import com.kivanov.diploma.services.oauth.YandexOauthApi20;
import lombok.NonNull;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutionException;


public class YandexService {
    OAuth20Service service;
    OAuth2AccessToken accessToken;

    public YandexService(){
        this.service = new ServiceBuilder("cbbe4732e7f64509b2c72f06e75b15cf")
                .apiSecret("39142b3043a14d59951ee3fec132a5a3")
                .defaultScope("cloud_api:disk.read")
                .callback("http://localhost:8080/" + WebUrls.OAUTH2_YANDEX)
                .build(YandexOauthApi20.instance());
    }

    public void doOauth(@NonNull String code) throws IOException, ExecutionException, InterruptedException {
        accessToken = service.getAccessToken(code);
    }

    public String getAuthorizationUrl(){
        return this.service.getAuthorizationUrl();
    }

    public String getOauthToken(){
        return accessToken.getAccessToken();
    }

    public YandexUserInfo getUserInfo(@NonNull String oauthToken) throws IOException {

        String urlRequest = "https://login.yandex.ru/info";

        URL url=new URL(urlRequest);
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
        Gson gson = new Gson();
        return gson.fromJson(response.toString(), YandexUserInfo.class);
    }
}

