package com.kivanov.diploma.services;

import com.kivanov.diploma.model.KeepFile;
import com.kivanov.diploma.model.KeepFileSourceComparator;
import com.kivanov.diploma.model.KeepProject;
import com.kivanov.diploma.model.KeepSource;
import com.kivanov.diploma.services.cloud.UrlConfiguration;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class FileSyncService {

    @Autowired
    UrlConfiguration urlConfiguration;

    @Autowired
    FileRepositoryService fileRepositoryService;

    @Autowired
    @Qualifier("CloudsFileService")
    FileService cloudsFileService;

    @Autowired
    @Qualifier("LocalFileService")
    FileService localFileService;

    public void syncLocalFiles(KeepProject project) {
        syncLocalFileStorage(project.getLocalSource());
        syncCloudFileStorage(project.getCloudSource());
    }

    public void initDataFromCloud(@NonNull KeepSource keepSource) {
        try {
            log.error("Attempt to init file recording from Local Storage '{}'", keepSource.getPath());
            Optional<KeepFile> rootKeepFile = fileRepositoryService.findRootOfSource(keepSource);
            if(rootKeepFile.isEmpty()) cloudsFileService.initRecordFiles(keepSource);
        } catch (Exception e) {
            log.error("Attempt to init file recording from Local Storage '{}' was fail with error:", keepSource.getPath());
            log.error(e.getMessage());
        }
    }

    public void initDataFromLocal(@NonNull KeepSource keepSource) {
        try {
            log.error("Attempt to init file recording from Local Storage '{}'", keepSource.getPath());
            Optional<KeepFile> rootKeepFile = fileRepositoryService.findRootOfSource(keepSource);
            if(rootKeepFile.isEmpty()) localFileService.initRecordFiles(keepSource);
        } catch (Exception e) {
            log.error("Attempt to init file recording from Local Storage '{}' was fail with error:", keepSource.getPath());
            log.error(e.getMessage());
        }
    }

    private void syncLocalFileStorage(KeepSource source) {
        initDataFromLocal(source);
        KeepFileSourceComparator keepFileSourceComparator = new KeepFileSourceComparator();
        fileRepositoryService.findRootOfSource(source).ifPresentOrElse(keepFileFromDb -> {
            KeepFile rootLocalKeepFile = KeepFile.Root(null);
            keepFileSourceComparator.compareLeftToRightSource(
                    (file) -> localFileService.collectKeepFilesByRootFile(file, source),
                    (file) -> Objects.isNull(file.getId()) ? new ArrayList<>() : fileRepositoryService.findNotDeletedFilesByParent(file),
                    rootLocalKeepFile,
                    keepFileFromDb,
                    source);
        }, () -> log.error("Root Keep File was not found in DB for Source {}", source));

    }

    private void syncCloudFileStorage(KeepSource source) {
        initDataFromCloud(source);
        KeepFileSourceComparator keepFileSourceComparator = new KeepFileSourceComparator();
        fileRepositoryService.findRootOfSource(source).ifPresentOrElse(keepFileFromDb -> {
                    KeepFile initKeePFileRoot = KeepFile.Root(null);
                    keepFileSourceComparator.compareLeftToRightSource(
                            (file) -> cloudsFileService.collectKeepFilesByRootFile(file, source),
                            (file) -> Objects.isNull(file.getId()) ? new ArrayList<>() : fileRepositoryService.findNotDeletedFilesByParent(file),
                            initKeePFileRoot,
                            keepFileFromDb,
                            source);
                }, () -> log.error("Root Keep File was not found in DB for Source {}", source));
    }

    private void syncCloudAndLocalStorage(KeepSource localSource, KeepSource cloudSource) {
        KeepFileSourceComparator keepFileSourceComparator = new KeepFileSourceComparator();

        KeepFile localRoot = fileRepositoryService.findRootOfSource(localSource).get();
        KeepFile cloudRoot = fileRepositoryService.findRootOfSource(cloudSource).get();
        keepFileSourceComparator.compareLeftToRightSource(
                (file) -> Objects.isNull(file.getId()) ? new ArrayList<>() : fileRepositoryService.findNotDeletedFilesByParent(file),
                (file) -> Objects.isNull(file.getId()) ? new ArrayList<>() : fileRepositoryService.findNotDeletedFilesByParent(file),
                localRoot,
                cloudRoot,
                cloudSource);
    }
}
