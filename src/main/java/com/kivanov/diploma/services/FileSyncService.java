package com.kivanov.diploma.services;

import com.kivanov.diploma.model.*;
import com.kivanov.diploma.services.cloud.UrlConfiguration;
import com.kivanov.diploma.services.localstorage.LocalFileReadingException;
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

    public SyncKeepFileData syncLocalFiles(KeepProject project) throws FileDealingException {
        syncLocalFileStorage(project.getLocalSource());
        syncCloudFileStorage(project.getCloudSource());
        return syncCloudAndLocalStorage(project.getLocalSource(), project.getCloudSource());
    }

    public void initDataFromCloud(@NonNull KeepSource keepSource) throws FileDealingException {
        log.error("Attempt to init file recording from Local Storage '{}'", keepSource.getPath());
        Optional<KeepFile> rootKeepFile = fileRepositoryService.findRootOfSource(keepSource);
        if(rootKeepFile.isEmpty()) cloudsFileService.initFindAndSaveAllFiles(keepSource);
    }

    public void initDataFromLocal(@NonNull KeepSource keepSource) throws FileDealingException {
        log.error("Attempt to init file recording from Local Storage '{}'", keepSource.getPath());
        Optional<KeepFile> rootKeepFile = fileRepositoryService.findRootOfSource(keepSource);
        if(rootKeepFile.isEmpty()) localFileService.initFindAndSaveAllFiles(keepSource);
    }

    private void syncLocalFileStorage(KeepSource source) throws FileDealingException {
        initDataFromLocal(source);
        KeepFileSourceComparator keepFileSourceComparator = new KeepFileSourceComparator();
        fileRepositoryService.findRootOfSource(source).ifPresentOrElse(keepFileFromDb -> {
            KeepFile rootLocalKeepFile = KeepFile.Root(null);
            keepFileSourceComparator.compareLeftToRightSource(
                    (file) -> {
                        try {
                            return localFileService.collectKeepFilesByRootFile(file, source);
                        } catch (FileDealingException e) {
                            throw new RuntimeException(e);
                        }
                    },
                    (file) -> Objects.isNull(file.getId()) ? new ArrayList<>() : fileRepositoryService.findNotDeletedFilesByParent(file),
                    rootLocalKeepFile,
                    keepFileFromDb,
                    source);
        }, () -> log.error("Root Keep File was not found in DB for Source {}", source));
    }

    private void syncCloudFileStorage(KeepSource source) throws FileDealingException {
        initDataFromCloud(source);
        KeepFileSourceComparator keepFileSourceComparator = new KeepFileSourceComparator();
        fileRepositoryService.findRootOfSource(source).ifPresentOrElse(keepFileFromDb -> {
                    KeepFile initKeePFileRoot = KeepFile.Root(null);
                    keepFileSourceComparator.compareLeftToRightSource(
                            (file) -> {
                                try {
                                    return cloudsFileService.collectKeepFilesByRootFile(file, source);
                                } catch (FileDealingException e) {
                                    throw new RuntimeException(e);
                                }
                            },
                            (file) -> Objects.isNull(file.getId()) ? new ArrayList<>() : fileRepositoryService.findNotDeletedFilesByParent(file),
                            initKeePFileRoot,
                            keepFileFromDb,
                            source);
                }, () -> log.error("Root Keep File was not found in DB for Source {}", source));
    }

    private SyncKeepFileData syncCloudAndLocalStorage(KeepSource localSource, KeepSource cloudSource) {
        KeepFileSourceComparator keepFileSourceComparator = new KeepFileSourceComparator();

        KeepFile localRoot = fileRepositoryService.findRootOfSource(localSource).get();
        KeepFile cloudRoot = fileRepositoryService.findRootOfSource(cloudSource).get();
        keepFileSourceComparator.compareLeftToRightSource(
                (file) -> Objects.isNull(file.getId()) ? new ArrayList<>() : fileRepositoryService.findNotDeletedFilesByParent(file),
                (file) -> Objects.isNull(file.getId()) ? new ArrayList<>() : fileRepositoryService.findNotDeletedFilesByParent(file),
                localRoot,
                cloudRoot,
                cloudSource);
        SyncKeepFileData syncData = new SyncKeepFileData();
        syncData.setNewLocalFiles(keepFileSourceComparator.leftNotMatchedFileList);
        syncData.setNewCloudFiles(keepFileSourceComparator.rightNotMatchedFileList);
        syncData.setModifiedFiles(keepFileSourceComparator.modifiedFileList);
        return syncData;
    }
}
