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

    public KeepFileSourceComparator keepFileSourceComparator = new KeepFileSourceComparator();

    public void syncLocalFiles(KeepProject project) throws IOException {
        syncLocalFileStorage(project.getLocalSource());
        //syncCloudFileStorage(project.getCloudSource());
    }

    private void syncLocalFileStorage(KeepSource source) {
        KeepFile dbRoot = fileRepositoryService.findRootOfSource(source);
        KeepFile rootLocalKeepFile = new KeepFile();
        rootLocalKeepFile.setName("");
        keepFileSourceComparator.compareLeftToRightSource(
                (file) -> {
                            StringBuffer stringPathBuilder = new StringBuffer(source.getPath()).append("/");
                            List<KeepFile> parents = new ArrayList<>();
                            buildLocalPathList(parents, file);
                            parents.reversed().stream().filter(keepFile -> !keepFile.isRoot()).forEach(keepFile -> {
                                stringPathBuilder.append("/").append(keepFile.getName());
                            });
                            stringPathBuilder.append("/").append(file.getName());
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
        //keepFileSync.getLeftNotMatchedFileList().forEach(this::saveKeepFile);
        //keepFileSync.getRightNotMatchedFileList().forEach(this::saveKeepFileAsDeleted);
        //keepFileSync.getModifiedFileList().forEach(this::saveKeepFile);
    }

    private void syncCloudFileStorage(KeepSource source) {
        YandexFileHandler yandexFileHandler = new YandexFileHandler(new HttpRequestMaker(), urlConfiguration);

        KeepFile dbRoot = fileRepositoryService.findRootOfSource(source);
        KeepFile rootLocalKeepFile = new KeepFile();
        rootLocalKeepFile.setName("");
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
        KeepFile parent = keepFile.getParent();
        if (!Objects.isNull(parent)) {
            parents.add(parent);
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

    private void sync2(Function<KeepFile, List<KeepFile>> fileListProvider, KeepFile rootPath, KeepFile dbRoot, KeepSource source) {
        List<KeepFile> dbFiles = Objects.isNull(dbRoot.getId()) ? new ArrayList<>() : fileRepositoryService.findNotDeletedFilesByParent(dbRoot);
        List<KeepFile> sourcFileList = fileListProvider.apply(rootPath);
        sourcFileList.forEach(syncFile -> {
            Optional<KeepFile> dbFile = dbFiles.stream()
                    .filter(keepFile -> keepFile.getName().equals(syncFile.getName()) && keepFile.isDirectory()==syncFile.isDirectory()).findFirst();
            dbFile.ifPresentOrElse(keepFile -> {
                        if(!syncFile.isDirectory()) {
                            if(!keepFile.getSha256().equals(syncFile.getSha256())
                                    || !keepFile.getCreationDateTime().equals(syncFile.getCreationDateTime())
                                    || !keepFile.getModifiedDateTime().equals(syncFile.getModifiedDateTime())) {
                                keepFile.setCreationDateTime(syncFile.getCreationDateTime());
                                keepFile.setModifiedDateTime(syncFile.getModifiedDateTime());
                                keepFile.setSha256(syncFile.getSha256());
                                modifiedList.add(keepFile);
                            }
                        } else sync2(fileListProvider,
                                syncFile,
                                keepFile,
                                source);
                    },
                    () -> {
                        try {
                            KeepFile newKeepFile = syncFile.cloneNew();
                            newKeepFile.setParent(dbRoot);
                            newKeepFile.setSource(source);
                            newList.add(newKeepFile);
                        } catch (CloneNotSupportedException e) {
                            log.error(e.getMessage());
                        }
                        if(syncFile.isDirectory()) {
                            sync2(fileListProvider,
                                  syncFile,
                                  syncFile,
                                  source);
                        }
                    });
        });
        removedList.addAll(dbFiles.stream().filter(keepFile -> sourcFileList.stream().noneMatch(syncFile1 -> syncFile1.getName().equals(keepFile.getName()))).toList());
        removedList.forEach(keepFile -> keepFile.setDeleted(true));
    }


    private void sync(Path rootPath, KeepFile dbRoot, KeepSource source) {
        List<KeepFile> dbFiles = Objects.isNull(dbRoot.getId()) ? new ArrayList<>() : fileRepositoryService.findNotDeletedFilesByParent(dbRoot);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(rootPath)) {
            List<Path> pathList = new ArrayList<>();
            for (Path path : stream) {
                pathList.add(path);
                boolean isDirectory = Files.isDirectory(path);
                LocalDateTime modificationDateTime = LocalDateTime.ofInstant((Files.getLastModifiedTime(path)).toInstant(), ZoneId.systemDefault());
                LocalDateTime creationDateTime = LocalDateTime.ofInstant(((FileTime) Files.getAttribute(path, "creationTime")).toInstant(), ZoneId.systemDefault());

                Optional<KeepFile> syncFile = dbFiles.stream()
                        .filter(keepFile -> keepFile.getName().equals(path.getFileName().toString()) && keepFile.isDirectory()==isDirectory).findFirst();
                syncFile.ifPresentOrElse(keepFile -> {
                        if(!isDirectory) {
                            if(!keepFile.getSha256().equals(calculateSha256(path))
                                    || !keepFile.getCreationDateTime().equals(creationDateTime)
                                    || !keepFile.getModifiedDateTime().equals(modificationDateTime)) {
                                keepFile.setCreationDateTime(creationDateTime);
                                keepFile.setModifiedDateTime(modificationDateTime);
                                keepFile.setSha256(calculateSha256(path));
                                modifiedList.add(keepFile);
                            }
                        } else sync(path, keepFile, source);
                    },
                    () -> {
                        KeepFile newKeepFile = new KeepFile();
                        newKeepFile.setDirectory(isDirectory);
                        newKeepFile.setName(path.getFileName().toString());
                        if(!isDirectory) newKeepFile.setSha256(calculateSha256(path));
                        newKeepFile.setCreationDateTime(creationDateTime);
                        newKeepFile.setModifiedDateTime(modificationDateTime);
                        newKeepFile.setParent(dbRoot);
                        newKeepFile.setSource(source);
                        newList.add(newKeepFile);
                        if(isDirectory) {
                            sync(path, newKeepFile, source);
                        }
                    });
            }
            removedList.addAll(dbFiles.stream().filter(keepFile -> pathList.stream().noneMatch(path -> path.getFileName().toString().equals(keepFile.getName()))).toList());
            removedList.forEach(keepFile -> keepFile.setDeleted(true));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
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
    private List<CompareFile> checkDirectionDiff(List<CompareFile> left, List<CompareFile> right) {
        List<CompareFile> diffList = new java.util.ArrayList<>(List.copyOf(left));
        diffList.removeAll(right);
        return diffList;
    }

    @Getter
    class CompareFile {

        private String sha5 = "";
        private String fileName;
        private LocalDateTime creationDateTime;
        private LocalDateTime modifiedDateTime;
        private boolean isDir = false;
        private KeepFile keepFile;

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof CompareFile)) return false;
            return ((CompareFile) object).getFileName().equals(this.fileName)
                    && ((CompareFile) object).getSha5().equals(this.sha5)
                    && ((CompareFile) object).getModifiedDateTime().equals(this.modifiedDateTime)
                    && ((CompareFile) object).getCreationDateTime().equals(this.creationDateTime)
                    && ((CompareFile) object).isDir() == (this.isDir);
        }
        public CompareFile(Path path) throws IOException {
            try {
                FileTime creationTime = (FileTime) Files.getAttribute(path, "creationTime");
                FileTime modificationTime = Files.getLastModifiedTime(path);
                fileName = path.getFileName().toString();
                creationDateTime = LocalDateTime.ofInstant(creationTime.toInstant(), ZoneId.systemDefault());
                modifiedDateTime = LocalDateTime.ofInstant(modificationTime.toInstant(), ZoneId.systemDefault());
                isDir = Files.isDirectory(path);
                if(!isDir) {
                    ByteSource byteSource = com.google.common.io.Files.asByteSource(path.toFile());
                    HashCode hc = byteSource.hash(Hashing.sha256());
                    sha5 = hc.toString();
                }
            } catch (IOException e) {
                log.error(e.getMessage());
            }
            FileTime creationTime = (FileTime) Files.getAttribute(path, "creationTime");
            FileTime modificationTime = Files.getLastModifiedTime(path);
            fileName = path.getFileName().toString();
            creationDateTime = LocalDateTime.ofInstant(creationTime.toInstant(), ZoneId.systemDefault());
            modifiedDateTime = LocalDateTime.ofInstant(modificationTime.toInstant(), ZoneId.systemDefault());
            isDir = Files.isDirectory(path);
            if(!isDir) {
                ByteSource byteSource = com.google.common.io.Files.asByteSource(path.toFile());
                HashCode hc = byteSource.hash(Hashing.sha256());
                sha5 = hc.toString();
            }
        }

        public CompareFile(KeepFile keepFile) {
            isDir = keepFile.isDirectory();
            fileName = keepFile.getName();
            creationDateTime = keepFile.getCreationDateTime();
            modifiedDateTime = keepFile.getModifiedDateTime();
            this.keepFile = keepFile;
        }

        public void setKeepFile(KeepFile keepFile) {
            this.keepFile = keepFile;
        }

    }


}
