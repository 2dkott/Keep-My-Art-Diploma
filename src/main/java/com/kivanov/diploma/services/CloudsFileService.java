package com.kivanov.diploma.services;

import com.kivanov.diploma.model.KeepFile;
import com.kivanov.diploma.model.KeepSource;
import com.kivanov.diploma.model.SourceType;
import com.kivanov.diploma.services.cloud.CloudFileRetrievalService;
import com.kivanov.diploma.services.cloud.HttpRequestMaker;
import com.kivanov.diploma.services.cloud.UrlConfiguration;
import com.kivanov.diploma.services.cloud.yandex.YandexFileRetrieval;
import jakarta.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;

@Slf4j
public class CloudsFileService{

    FileRepositoryService fileRepositoryService;

    Map<SourceType, CloudFileRetrievalService> retrievalServices = new HashMap<>();


    public CloudsFileService(FileRepositoryService fileRepositoryService, HttpRequestMaker httpRequestMaker, UrlConfiguration urlConfiguration) {
        this.fileRepositoryService = fileRepositoryService;
        retrievalServices.put(SourceType.YANDEX, new YandexFileRetrieval(httpRequestMaker, urlConfiguration));
    }

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
                    log.error("Fetching Data from Cloud of Source {} was failed:", cloudSources);
                    log.error(e.getMessage());
                }
            }
        });
    }

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

    public void uploadFiles(List<KeepFile> keepFiles, KeepSource keepSource) {
        retrievalServices.keySet().forEach(cloudFileServiceKey -> {
            if(cloudFileServiceKey.equals(keepSource.getType())) {
                CloudFileRetrievalService cloudFileService = retrievalServices.get(cloudFileServiceKey);
                keepFiles.forEach(keepFile -> {
                    try {
                        createParentDirectory(keepFile.getParent(), keepSource, cloudFileService);
                        cloudFileService.uploadFile(keepFile,keepSource);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        });
    }

    private void createParentDirectory(KeepFile keepFile, KeepSource keepSource, CloudFileRetrievalService cloudFileService) throws IOException {
        Optional<KeepFile> cloudDir = fileRepositoryService.findFileByPathIdAndSource(keepFile, keepSource);
        if(cloudDir.isEmpty()) {
            createParentDirectory(keepFile.getParent(), keepSource, cloudFileService);
            cloudFileService.createDirectory(keepFile, keepSource);
        }
    }

    public void downloadFiles(@NotEmpty List<KeepFile> keepFiles, KeepSource localKeepSource) {
        retrievalServices.keySet().forEach(cloudFileServiceKey -> {
            if(cloudFileServiceKey.equals(keepFiles.get(0).getSource().getType())) {
                CloudFileRetrievalService cloudFileService = retrievalServices.get(cloudFileServiceKey);
                keepFiles.forEach(keepFile -> {
                    try {
                        cloudFileService.download(keepFile, localKeepSource);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        });
    }
}
