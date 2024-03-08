package com.kivanov.diploma.services;

import com.kivanov.diploma.persistence.KeepFileRepository;
import com.kivanov.diploma.services.cloud.HttpRequestMaker;
import com.kivanov.diploma.services.cloud.UrlConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServicesConfiguration {

    @Autowired
    KeepFileRepository fileRepository;

    @Autowired
    UrlConfiguration urlConfiguration;

    @Bean("LocalFileService")
    public FileService localFileService() {
        return new LocalFileService(fileRepository);
    }

    @Bean("CloudsFileService")
    public FileService cloudsFileService() {
        return new CloudsFileService(fileRepository, new HttpRequestMaker(), urlConfiguration);
    }
}
