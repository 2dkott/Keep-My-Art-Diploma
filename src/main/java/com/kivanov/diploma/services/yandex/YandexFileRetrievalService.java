package com.kivanov.diploma.services.yandex;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.kivanov.diploma.model.KeepFile;
import com.kivanov.diploma.model.KeepSource;
import com.kivanov.diploma.persistence.KeepFileRepository;
import com.kivanov.diploma.services.CloudFileRetrievalService;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class YandexFileRetrievalService implements CloudFileRetrievalService {

    private KeepFileRepository keepFileRepository;

    public void recordFileData(KeepSource source) throws IOException {
        KeepFile parent = new KeepFile();
        parent.setSource(source);
        keepFileRepository.save(parent);
        ConnectionData connectionData = new ConnectionData();
        connectionData.setUrl("https://cloud-api.yandex.net/v1/disk/resources?path="+source.getPath());
        connectionData.setOauthToken(source.getUserToken());
        read(connectionData, source, parent);
    }

    private void read(ConnectionData connectionData, KeepSource source, KeepFile parent) throws IOException {
        List<YandexFile> yandexFiles = getFilesFromResource(connectionData);
        for (YandexFile yandexFile : yandexFiles) {
            KeepFile file = new KeepFile();
            file.setName(yandexFile.getName());
            file.setParent(parent);
            file.setDirectory(yandexFile.isDirectory());
            file.setDeleted(false);
            file.setCreationTime(yandexFile.getCreated());
            file.setUpdateTime(yandexFile.getUpdated());
            file.setSource(source);
            keepFileRepository.save(file);
            if (yandexFile.isDirectory()) {
                connectionData.setUrl(connectionData.getUrl() + "/" + yandexFile.getFileName());
                read(connectionData, source, file);
            }
        }

    }

    private URLConnection openConnection(ConnectionData connectionData) throws IOException {
        URL url=new URL(connectionData.getUrl());
        URLConnection urlConnection = url.openConnection();
        HttpsURLConnection httpsConnection = (HttpsURLConnection) urlConnection;
        httpsConnection.setRequestMethod("GET");
        httpsConnection.setRequestProperty("Authorization", "OAuth " + connectionData.getOauthToken());
        httpsConnection.connect();
        return httpsConnection;
    }

    private String getResponse(HttpsURLConnection connection) throws IOException {
        BufferedReader responseStream = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = responseStream.readLine()) != null) {
            response.append(inputLine);
        }
        responseStream.close();
        return response.toString();
    }

    List<YandexFile> getFilesFromResource(ConnectionData connectionData) throws IOException {
        HttpsURLConnection connection = (HttpsURLConnection) openConnection(connectionData);
        String response = getResponse(connection);
        Gson gson = new Gson();
        JsonElement jsonWithFiles = new JsonParser().parse(response.toString()).getAsJsonObject().get("_embedded").getAsJsonObject().get("items").getAsJsonArray();
        return gson.fromJson(jsonWithFiles, new TypeToken<ArrayList<YandexFile>>(){}.getType());
    }

    @Data
    class ConnectionData {
        private String url;
        private String oauthToken;
    }

}


