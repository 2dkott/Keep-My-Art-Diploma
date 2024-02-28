package com.kivanov.diploma.services;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;
import com.kivanov.diploma.model.WebUrls;
import com.kivanov.diploma.services.oauth.YandexOauthApi20;
import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutionException;


@Component
public class YandexService {
    OAuth20Service service;
    OAuth2AccessToken accessToken;

    @Value("${services.api.yandex.key}")
    private String apiKey;

    @Value("${services.api.yandex.secret}")
    private String apiSecret;

    @Value("${services.api.yandex.scope}")
    private String apiScope;

    @PostConstruct
    public void init() {
        this.service = new ServiceBuilder(apiKey)
                .apiSecret(apiSecret)
                .defaultScope(apiScope)
                .callback("http://localhost:8080/" + WebUrls.OAUTH2_YANDEX)
                .build(YandexOauthApi20.instance());
    }

    public void doOauth(String code) throws IOException, ExecutionException, InterruptedException {
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

