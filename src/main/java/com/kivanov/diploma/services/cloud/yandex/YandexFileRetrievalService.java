package com.kivanov.diploma.services.cloud.yandex;

import com.kivanov.diploma.model.KeepFile;
import com.kivanov.diploma.model.KeepSource;
import com.kivanov.diploma.services.cloud.CloudFileRetrievalService;
import com.kivanov.diploma.services.cloud.ConnectionData;
import com.kivanov.diploma.services.cloud.HttpRequestMaker;
import com.kivanov.diploma.services.cloud.JsonMapper;
import com.kivanov.diploma.services.cloud.UrlConfiguration;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class YandexFileRetrievalService implements CloudFileRetrievalService {


    private final HttpRequestMaker httpRequestMaker;
    private final UrlConfiguration urlConfiguration;

    public List<KeepFile> fetchCLoudFileToKeepFiles(KeepFile keepFileRoot, KeepSource source, boolean flat) throws IOException {
        List<KeepFile> fileList = new ArrayList<>();
        ConnectionData connectionData = new ConnectionData();
        connectionData.setUrl(urlConfiguration.getYandex().getRoot() + source.getPath() + keepFileRoot.getPathId());
        connectionData.setOauthToken(source.getUserToken());
        if (flat) {
            flatSearchAndMapFilesToKeepFileList(connectionData, keepFileRoot, fileList);
        } else {
            searchAndMapFilesToKeepFileList(connectionData, keepFileRoot, fileList);
        }
        return fileList;
    }

    public void searchAndMapFilesToKeepFileList(ConnectionData connectionData, KeepFile parent, List<KeepFile> keepFilesStorage) throws IOException {
        List<YandexFile> yandexFiles = getFilesFromResource(connectionData);
        for (YandexFile yandexFile : yandexFiles) {
            KeepFile keepFile = yandexFile.mapToKeepFile(parent);
            keepFilesStorage.add(keepFile);
            if (yandexFile.isDirectory()) {
                connectionData.setUrl(connectionData.getUrl() + "/" + yandexFile.getName());
                searchAndMapFilesToKeepFileList(connectionData, keepFile, keepFilesStorage);
            }
        }
    }

    public void flatSearchAndMapFilesToKeepFileList(ConnectionData connectionData, KeepFile parent, List<KeepFile> keepFilesStorage) throws IOException {
        List<YandexFile> yandexFiles = getFilesFromResource(connectionData);
        for (YandexFile yandexFile : yandexFiles) {
            KeepFile keepFile = yandexFile.mapToKeepFile(parent);
            keepFilesStorage.add(keepFile);
        }
    }

    public List<YandexFile> getFilesFromResource(ConnectionData connectionData) throws IOException {
        String jsonResponse = httpRequestMaker.getResponse(connectionData.getUrl(), connectionData.getOauthToken());
        return JsonMapper.mapJsonToYandexFiles(jsonResponse);
    }
}