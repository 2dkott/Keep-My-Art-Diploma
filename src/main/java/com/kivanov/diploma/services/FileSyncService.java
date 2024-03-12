package com.kivanov.diploma.services;

import com.google.common.collect.Lists;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;
import com.kivanov.diploma.model.KeepFile;
import com.kivanov.diploma.model.KeepFileSourceComparator;
import com.kivanov.diploma.model.KeepProject;
import com.kivanov.diploma.model.KeepSource;
import com.kivanov.diploma.services.cloud.HttpRequestMaker;
import com.kivanov.diploma.services.cloud.UrlConfiguration;
import com.kivanov.diploma.services.cloud.yandex.YandexFileHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;

@Slf4j
@Component
public class FileSyncService {

    @Autowired
    UrlConfiguration urlConfiguration;

    @Autowired
    FileRepositoryService fileRepositoryService;

    public List<KeepFile> newList = new ArrayList<>();
    public List<KeepFile> removedList = new ArrayList<>();
    public List<KeepFile> modifiedList = new ArrayList<>();

    public void syncLocalFiles(KeepProject project) throws IOException {
        syncLocalFileStorage(project.getLocalSource());
        //syncCloudFileStorage(project.getCloudSource());
    }

    private void syncLocalFileStorage(KeepSource source) {
        KeepFileSourceComparator keepFileSourceComparator = new KeepFileSourceComparator();
        KeepFile dbRoot = fileRepositoryService.findRootOfSource(source);
        KeepFile rootLocalKeepFile = KeepFile.Root(source);
        keepFileSourceComparator.compareLeftToRightSource(
                (file) -> {
                            StringBuffer stringPathBuilder = new StringBuffer(source.getPath()).append("/");
                            List<KeepFile> parents = new ArrayList<>();
                            buildLocalPathList(parents, file);
                            parents.reversed().stream().filter(keepFile -> !keepFile.isRoot()).forEach(keepFile -> {
                                stringPathBuilder.append("/").append(keepFile.getName());
                            });
                            log.info("Build File Root Path {}", stringPathBuilder);
                            try {
                                Path parentPath = Paths.get(stringPathBuilder.toString());
                                if(!Files.exists(parentPath)) { log.info("Read File Root Path {} is not exist", stringPathBuilder); return new ArrayList<>(); }
                                log.info("Read File Root Path {}", stringPathBuilder);
                                return Lists.newArrayList(Files.newDirectoryStream(parentPath)).stream().map(path -> {
                                    try {
                                        KeepFile keepFile = new KeepFile();
                                        keepFile.setName(path.getFileName().toString());
                                        keepFile.setModifiedDateTime(LocalDateTime.ofInstant((Files.getLastModifiedTime(path)).toInstant(), ZoneId.systemDefault()));
                                        keepFile.setCreationDateTime(LocalDateTime.ofInstant(((FileTime) Files.getAttribute(path, "creationTime")).toInstant(), ZoneId.systemDefault()));
                                        keepFile.setSha256(calculateSha256(path));
                                        keepFile.setDirectory(Files.isDirectory(path));
                                        keepFile.setParent(file);
                                        return keepFile;
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }).toList();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                },
                (file) -> Objects.isNull(file.getId()) ? new ArrayList<>() : fileRepositoryService.findNotDeletedFilesByParent(file),
                rootLocalKeepFile,
                dbRoot,
                source);
        log.info(keepFileSourceComparator.modifiedFileList.toString());
        log.info(keepFileSourceComparator.leftNotMatchedFileList.toString());
        log.info(keepFileSourceComparator.rightNotMatchedFileList.toString());
    }

    private void syncCloudFileStorage(KeepSource source) {
        KeepFileSourceComparator keepFileSourceComparator = new KeepFileSourceComparator();
        YandexFileHandler yandexFileHandler = new YandexFileHandler(new HttpRequestMaker(), urlConfiguration);

        KeepFile dbRoot = fileRepositoryService.findRootOfSource(source);
        KeepFile rootLocalKeepFile = KeepFile.Root(source);
        keepFileSourceComparator.compareLeftToRightSource(
                (file) -> {
                    StringBuffer stringPathBuilder = new StringBuffer("/");
                    List<KeepFile> parents = new ArrayList<>();
                    buildLocalPathList(parents, file);
                    parents.reversed().stream().filter(keepFile -> !keepFile.isRoot()).forEach(keepFile -> {
                        stringPathBuilder.append(keepFile.getName()).append("/");
                    });
                    stringPathBuilder.append(file.getName());
                    try {
                        return yandexFileHandler.getFileData(stringPathBuilder.toString(), source).stream().map(yandexFile -> yandexFile.mapToKeepFile(rootLocalKeepFile,source)).toList();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                (file) -> Objects.isNull(file.getId()) ? new ArrayList<>() : fileRepositoryService.findNotDeletedFilesByParent(file),
                rootLocalKeepFile,
                dbRoot,
                source);
        //keepFileSync.getLeftNotMatchedFileList().forEach(this::saveKeepFile);
        //keepFileSync.getRightNotMatchedFileList().forEach(this::saveKeepFileAsDeleted);
        //keepFileSync.getModifiedFileList().forEach(this::saveKeepFile);
    }

    private void syncCloudAndLocalStorage(KeepSource localSource, KeepSource cloudSource) {
        KeepFileSourceComparator keepFileSourceComparator = new KeepFileSourceComparator();
        YandexFileHandler yandexFileHandler = new YandexFileHandler(new HttpRequestMaker(), urlConfiguration);

        KeepFile localRoot = fileRepositoryService.findRootOfSource(localSource);
        KeepFile cloudRoot = fileRepositoryService.findRootOfSource(cloudSource);
        keepFileSourceComparator.compareLeftToRightSource(
                (file) -> Objects.isNull(file.getId()) ? new ArrayList<>() : fileRepositoryService.findNotDeletedFilesByParent(file),
                (file) -> Objects.isNull(file.getId()) ? new ArrayList<>() : fileRepositoryService.findNotDeletedFilesByParent(file),
                localRoot,
                cloudRoot,
                cloudSource);
        //keepFileSync.getLeftNotMatchedFileList().forEach(this::saveKeepFile);
        //keepFileSync.getRightNotMatchedFileList().forEach(this::saveKeepFileAsDeleted);
        //keepFileSync.getModifiedFileList().forEach(this::saveKeepFile);
    }

    private void buildLocalPathList(List<KeepFile> parents, KeepFile keepFile) {
        parents.add(keepFile);
        KeepFile parent = keepFile.getParent();
        if (!Objects.isNull(parent)) {
            //parents.add(parent);
            buildLocalPathList(parents, parent);
        }
    }

    private void saveKeepFile(KeepFile keepFile) {
        //if(Objects.isNull(keepFile.getParent().getId())) {
        //    saveKeepFile(keepFile.getParent());
        //}
        //fileRepositoryService.saveFile(keepFile);
    }

    private void saveKeepFileAsDeleted(KeepFile keepFile) {
        //keepFile.setDeleted(true);
        //fileRepositoryService.saveFile(keepFile);
        //keepFile.getChildren().forEach(keepFile1 -> saveKeepFileAsDeleted(keepFile1));
    }

    private String calculateSha256(Path path) {
        try{
            ByteSource byteSource = com.google.common.io.Files.asByteSource(path.toFile());
            HashCode hc = byteSource.hash(Hashing.sha256());
            return hc.toString();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }
}
