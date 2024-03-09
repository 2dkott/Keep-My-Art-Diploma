package com.kivanov.diploma.services;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;
import com.kivanov.diploma.model.KeepFile;
import com.kivanov.diploma.model.KeepSource;
import com.kivanov.diploma.persistence.KeepFileRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.TransientObjectException;
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

@Slf4j
@Component
public class FileSyncService {

    @Autowired
    KeepFileRepository keepFileRepository;

    public List<KeepFile> newList = new ArrayList<>();
    public List<KeepFile> removedList = new ArrayList<>();
    public List<KeepFile> modifiedList = new ArrayList<>();

    public void syncLocalFiles(KeepSource source) {
        KeepFile dbRoot = keepFileRepository.findKeepFileByNameAndSource(KeepFile.Root().getName(), source).get(0);
        Path localRootPath = Paths.get(source.getPath());
        sync(localRootPath, dbRoot, source);
    }

    private void sync(Path rootPath, KeepFile dbRoot, KeepSource source) {
        List<KeepFile> dbFiles = Objects.isNull(dbRoot.getId()) ? new ArrayList<>() : keepFileRepository.findKeepFileByParent(dbRoot);
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
            removedList.addAll(dbFiles.stream().filter(keepFile -> !pathList.stream().allMatch(path -> path.getFileName().toString().equals(keepFile.getName()))).toList());
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
