package com.kivanov.diploma.services;

import com.kivanov.diploma.persistence.KeepFileRepository;
import com.kivanov.diploma.services.yandex.YandexFileRetrievalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudConfiguration {

    @Autowired
    KeepFileRepository keepFileRepository;

    @Bean(name = "YandexFileService")
    public CloudFileRetrievalService yandexCloudFileService() {
        return new YandexFileRetrievalService(keepFileRepository);
    }
}
