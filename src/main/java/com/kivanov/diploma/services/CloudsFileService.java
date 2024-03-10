package com.kivanov.diploma.services;

import com.kivanov.diploma.model.KeepSource;
import com.kivanov.diploma.model.SourceType;
import com.kivanov.diploma.persistence.KeepFileRepository;
import com.kivanov.diploma.services.cloud.HttpRequestMaker;
import com.kivanov.diploma.services.cloud.UrlConfiguration;
import com.kivanov.diploma.services.cloud.yandex.YandexFileRetrievalService;

import java.io.IOException;

public class CloudsFileService implements FileService{

    CloudFileRetrievalService yandexFileService;


    public CloudsFileService(FileRepositoryService fileRepositoryService, HttpRequestMaker httpRequestMaker, UrlConfiguration urlConfiguration) {
        yandexFileService = new YandexFileRetrievalService(fileRepositoryService, httpRequestMaker, urlConfiguration);
    }


    @Override
    public void recordFiles(KeepSource cloudSources) {
        try {
            if(cloudSources.getType().equals(SourceType.YANDEX)) {
                yandexFileService.recordFileData(cloudSources);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
