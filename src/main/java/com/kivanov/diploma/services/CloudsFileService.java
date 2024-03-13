package com.kivanov.diploma.services;

import com.kivanov.diploma.model.KeepFile;
import com.kivanov.diploma.model.KeepSource;
import com.kivanov.diploma.model.SourceType;
import com.kivanov.diploma.services.cloud.CloudFileRetrievalService;
import com.kivanov.diploma.services.cloud.HttpRequestMaker;
import com.kivanov.diploma.services.cloud.UrlConfiguration;
import com.kivanov.diploma.services.cloud.yandex.YandexFileRetrievalService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class CloudsFileService implements FileService{

    FileRepositoryService fileRepositoryService;

    Map<SourceType, CloudFileRetrievalService> retrievalServices = new HashMap<>();


    public CloudsFileService(FileRepositoryService fileRepositoryService, HttpRequestMaker httpRequestMaker, UrlConfiguration urlConfiguration) {
        this.fileRepositoryService = fileRepositoryService;
        retrievalServices.put(SourceType.YANDEX, new YandexFileRetrievalService(httpRequestMaker, urlConfiguration));
    }

    @Override
    public void initFindAndSaveAllFiles(KeepSource cloudSources) {
        log.info("Choosing cloud to fetch file data for Source {}", cloudSources);
        retrievalServices.keySet().forEach(cloudFileServiceKey -> {
            if(cloudFileServiceKey.equals(cloudSources.getType())) {
                CloudFileRetrievalService cloudFileService = retrievalServices.get(cloudFileServiceKey);
                try {
                        log.info("Initiate getting data from Cloud {}", cloudSources.getType());
                        KeepFile rootKeepFile = fileRepositoryService.saveRoot(cloudSources);
                        log.info("Root File id:{} is saved", rootKeepFile.getId());
                        List<KeepFile> fileList = cloudFileService.fetchCLoudFileToKeepFiles(rootKeepFile, cloudSources, false);
                        log.info("File List was fetched {}", fileList);
                        fileRepositoryService.saveKeepFileListByParent(rootKeepFile, fileList);
                        log.info("File List was saved in DB");
                } catch (IOException e) {
                    log.info("Fetching Data from Cloud of Source {} was failed:", cloudSources);
                    log.info(e.getMessage());
                }
            }
        });
    }

    @Override
    public List<KeepFile> collectKeepFilesByRootFile(KeepFile rootPath, KeepSource keepSource){
        List<KeepFile> keepFileList = new ArrayList<>();
        retrievalServices.keySet().forEach(cloudFileServiceKey -> {
            if(cloudFileServiceKey.equals(keepSource.getType())) {
                CloudFileRetrievalService cloudFileService = retrievalServices.get(cloudFileServiceKey);
                try {
                    keepFileList.addAll(cloudFileService.fetchCLoudFileToKeepFiles(rootPath, keepSource, true));
                } catch (IOException e) {
                    log.info("Fetching Data from Cloud of Source {} was failed:", keepSource.getType());
                    log.info(e.getMessage());
                }
            }
        });
        return keepFileList;
    }
}
