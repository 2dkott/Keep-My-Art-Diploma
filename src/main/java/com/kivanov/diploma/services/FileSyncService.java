package com.kivanov.diploma.services;

import com.kivanov.diploma.model.*;
import com.kivanov.diploma.services.cloud.UrlConfiguration;
import com.kivanov.diploma.services.localstorage.LocalFileService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    CloudsFileService cloudsFileService;

    @Autowired
    LocalFileService localFileService;

    public SyncKeepFileData syncLocalFiles(KeepProject project) throws FileDealingException {
        syncLocalFileStorage(project.getLocalSource());
        syncCloudFileStorage(project.getCloudSource());
        return syncCloudAndLocalStorage(project.getLocalSource(), project.getCloudSource());
    }

    public void initDataFromCloud(@NonNull KeepSource keepSource) throws FileDealingException {
        log.error("Attempt to init file recording from Cloud Storage '{}'", keepSource.getPath());
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
                    (leftFile, rightFile) -> leftFile.getSha256().equals(rightFile.getSha256()),
                    rootLocalKeepFile,
                    keepFileFromDb,
                    source);
        }, () -> log.error("Root Keep File was not found in DB for Source {}", source));
        keepFileSourceComparator.getLeftNotMatchedFileList().forEach(newKeepFile -> saveKeepFile(newKeepFile, source));
        keepFileSourceComparator.getRightNotMatchedFileList().forEach(deletedKeepFile -> {
            deletedKeepFile.setDeleted(true);
            fileRepositoryService.saveFile(deletedKeepFile);
        });
        keepFileSourceComparator.getModifiedFileList().forEach(keepFileKeepFilePair -> {
            KeepFile dbKeepFile = keepFileKeepFilePair.getRight();
            KeepFile localKeepFile = keepFileKeepFilePair.getLeft();
            dbKeepFile.setCreationDateTime(localKeepFile.getCreationDateTime());
            dbKeepFile.setModifiedDateTime(localKeepFile.getModifiedDateTime());
            dbKeepFile.setSha256(localKeepFile.getSha256());
            fileRepositoryService.saveFile(dbKeepFile);
        });
    }

    private KeepFile saveKeepFile(KeepFile keepFile, KeepSource keepSource) {
        Optional<KeepFile> dbKeepFile = fileRepositoryService.findFileByPathIdAndSource(keepFile, keepSource);
        if(dbKeepFile.isEmpty()) {
            KeepFile parent = saveKeepFile(keepFile.getParent(), keepSource);
            keepFile.setParent(parent);
            keepFile.setSource(keepSource);
            fileRepositoryService.saveFile(keepFile);
            return keepFile;
        }
        return dbKeepFile.get();
    }
    private void syncCloudFileStorage(KeepSource source) throws FileDealingException {
        initDataFromCloud(source);
        KeepFileSourceComparator keepFileSourceComparator = new KeepFileSourceComparator();
        fileRepositoryService.findRootOfSource(source).ifPresentOrElse(keepFileFromDb -> {
                    KeepFile initKeePFileRoot = KeepFile.Root(null);
                    keepFileSourceComparator.compareLeftToRightSource(
                            (file) -> cloudsFileService.collectKeepFilesByRootFile(file, source),
                            (file) -> Objects.isNull(file.getId()) ? new ArrayList<>() : fileRepositoryService.findNotDeletedFilesByParent(file),
                            (leftFile, rightFile) -> leftFile.getSha256().equals(rightFile.getSha256()),
                            initKeePFileRoot,
                            keepFileFromDb,
                            source);
                }, () -> log.error("Root Keep File was not found in DB for Source {}", source));
        keepFileSourceComparator.getLeftNotMatchedFileList().forEach(newKeepFile -> saveKeepFile(newKeepFile, source));
        keepFileSourceComparator.getRightNotMatchedFileList().forEach(deletedKeepFile -> {
            deletedKeepFile.setDeleted(true);
            fileRepositoryService.saveFile(deletedKeepFile);
        });
        keepFileSourceComparator.getModifiedFileList().forEach(keepFileKeepFilePair -> {
            KeepFile dbKeepFile = keepFileKeepFilePair.getRight();
            KeepFile cloudKeepFile = keepFileKeepFilePair.getLeft();
            dbKeepFile.setCreationDateTime(cloudKeepFile.getCreationDateTime());
            dbKeepFile.setModifiedDateTime(cloudKeepFile.getModifiedDateTime());
            dbKeepFile.setSha256(cloudKeepFile.getSha256());
            fileRepositoryService.saveFile(dbKeepFile);
        });
    }

    private SyncKeepFileData syncCloudAndLocalStorage(KeepSource localSource, KeepSource cloudSource) {
        KeepFileSourceComparator keepFileSourceComparator = new KeepFileSourceComparator();

        KeepFile localRoot = fileRepositoryService.findRootOfSource(localSource).get();
        KeepFile cloudRoot = fileRepositoryService.findRootOfSource(cloudSource).get();
        keepFileSourceComparator.compareLeftToRightSource(
                (file) -> Objects.isNull(file.getId()) ? new ArrayList<>() : fileRepositoryService.findNotDeletedFilesByParentAndSource(file, localSource),
                (file) -> Objects.isNull(file.getId()) ? new ArrayList<>() : fileRepositoryService.findNotDeletedFilesByParentAndSource(file, cloudSource),
                (leftFile, rightFile) -> leftFile.getSha256().equals(rightFile.getSha256()),
                localRoot,
                cloudRoot,
                cloudSource);
        SyncKeepFileData syncData = new SyncKeepFileData();
        syncData.setNewLocalFiles(keepFileSourceComparator.leftNotMatchedFileList);
        syncData.setNewCloudFiles(keepFileSourceComparator.rightNotMatchedFileList);
        syncData.setModifiedFiles(keepFileSourceComparator.modifiedFileList);
        return syncData;
    }

    public void uploadFiles(List<KeepFile> keepFiles, KeepSource source) {
        cloudsFileService.uploadFiles(keepFiles, source);
    }

    public void downloadFiles(List<KeepFile> keepFiles, KeepSource source) {
        localFileService.checkAndCreateDirectories(keepFiles, source);
        cloudsFileService.downloadFiles(keepFiles, source);
    }
}
