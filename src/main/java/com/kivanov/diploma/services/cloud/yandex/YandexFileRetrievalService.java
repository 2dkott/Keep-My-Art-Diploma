package com.kivanov.diploma.services.cloud.yandex;

import com.kivanov.diploma.model.KeepFile;
import com.kivanov.diploma.model.KeepSource;
import com.kivanov.diploma.persistence.KeepFileRepository;
import com.kivanov.diploma.services.CloudFileRetrievalService;
import com.kivanov.diploma.services.cloud.HttpRequestMaker;
import com.kivanov.diploma.services.cloud.JsonMapper;
import com.kivanov.diploma.services.cloud.UrlConfiguration;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.IOException;
import java.util.List;

@AllArgsConstructor
public class YandexFileRetrievalService implements CloudFileRetrievalService {


    private final KeepFileRepository keepFileRepository;
    private final HttpRequestMaker httpRequestMaker;
    private final UrlConfiguration urlConfiguration;

    public void recordFileData(KeepSource source) throws IOException {
        KeepFile parent = new KeepFile();
        parent.setName("root");
        parent.setSource(source);
        keepFileRepository.save(parent);
        ConnectionData connectionData = new ConnectionData();
        connectionData.setUrl(urlConfiguration.getYandex().getRoot() + source.getPath());
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

    private List<YandexFile> getFilesFromResource(ConnectionData connectionData) throws IOException {
        String jsonResponse = httpRequestMaker.getResponse(connectionData.url, connectionData.oauthToken);
        return JsonMapper.mapJsonToYandexFiles(jsonResponse);
    }

    @Data
    class ConnectionData {
        private String url;
        private String oauthToken;
    }
}