package com.kivanov.diploma.services.cloud.yandex;

import com.kivanov.diploma.model.KeepFile;
import com.kivanov.diploma.model.KeepSource;
import com.kivanov.diploma.services.CloudFileRetrievalService;
import com.kivanov.diploma.services.FileRepositoryService;
import com.kivanov.diploma.services.cloud.ConnectionData;
import com.kivanov.diploma.services.cloud.HttpRequestMaker;
import com.kivanov.diploma.services.cloud.JsonMapper;
import com.kivanov.diploma.services.cloud.UrlConfiguration;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.List;

@AllArgsConstructor
public class YandexFileRetrievalService implements CloudFileRetrievalService {


    private FileRepositoryService fileRepositoryService;
    private final HttpRequestMaker httpRequestMaker;
    private final UrlConfiguration urlConfiguration;

    public void recordFileData(KeepSource source) throws IOException {
        KeepFile parent = fileRepositoryService.saveRoot(source);
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
            file.setCreationDateTime(yandexFile.getCreatedDateTime());
            file.setModifiedDateTime(yandexFile.getModifiedDateTime());
            file.setSha256(yandexFile.getSha256());
            file.setSource(source);
            fileRepositoryService.saveFile(file);
            if (yandexFile.isDirectory()) {
                connectionData.setUrl(connectionData.getUrl() + "/" + yandexFile.getName());
                read(connectionData, source, file);
            }
        }
    }

    public List<YandexFile> getFilesFromResource(ConnectionData connectionData) throws IOException {
        String jsonResponse = httpRequestMaker.getResponse(connectionData.getUrl(), connectionData.getOauthToken());
        return JsonMapper.mapJsonToYandexFiles(jsonResponse);
    }
}