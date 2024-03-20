package com.kivanov.diploma.services;

import com.kivanov.diploma.persistence.KeepFileRepository;
import com.kivanov.diploma.services.cloud.HttpRequestMaker;
import com.kivanov.diploma.services.cloud.UrlConfiguration;
import com.kivanov.diploma.services.localstorage.LocalFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServicesConfiguration {

    @Autowired
    KeepFileRepository fileRepository;

    @Autowired
    UrlConfiguration urlConfiguration;

    @Autowired
    FileRepositoryService fileRepositoryService;

    @Bean
    public LocalFileService localFileService() {
        return new LocalFileService(fileRepositoryService);
    }

    @Bean
    public CloudsFileService cloudsFileService() {
        return new CloudsFileService(fileRepositoryService, new HttpRequestMaker(), urlConfiguration);
    }
}
