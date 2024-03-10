package com.kivanov.diploma.services.cloud.yandex;

import com.kivanov.diploma.model.KeepSource;
import com.kivanov.diploma.services.cloud.ConnectionData;
import com.kivanov.diploma.services.cloud.HttpRequestMaker;
import com.kivanov.diploma.services.cloud.JsonMapper;
import com.kivanov.diploma.services.cloud.UrlConfiguration;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.List;

@AllArgsConstructor
public class YandexFileHandler {

    private final HttpRequestMaker httpRequestMaker;
    private final UrlConfiguration urlConfiguration;

    public List<YandexFile> getFileData(String root, KeepSource source) throws IOException {
        ConnectionData connectionData = new ConnectionData();
        connectionData.setUrl(getYandexRoot() + source.getPath() + root);
        connectionData.setOauthToken(source.getUserToken());
        return getFilesFromResource(connectionData);
    }

    public List<YandexFile> getFilesFromResource(ConnectionData connectionData) throws IOException {
        String jsonResponse = httpRequestMaker.getResponse(connectionData.getUrl(), connectionData.getOauthToken());
        return JsonMapper.mapJsonToYandexFiles(jsonResponse);
    }

    public String getYandexRoot() {
        return urlConfiguration.getYandex().getRoot();
    }
}
