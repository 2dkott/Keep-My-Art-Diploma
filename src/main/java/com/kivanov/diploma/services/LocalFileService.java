package com.kivanov.diploma.services;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;
import com.kivanov.diploma.model.KeepFile;
import com.kivanov.diploma.model.KeepSource;
import com.kivanov.diploma.persistence.KeepFileRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@AllArgsConstructor
public class LocalFileService implements FileService{

    private KeepFileRepository repository;

    @Override
    public void recordFiles(KeepSource source) throws IOException {
        KeepFile rootKeepFile = KeepFile.Root();
        rootKeepFile.setSource(source);
        repository.save(rootKeepFile);
        Path root = Paths.get(source.getPath());
        read(source, root, rootKeepFile);
    }

    public void read(KeepSource source, Path rootPath, KeepFile parent) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(rootPath)) {
            for (Path path : stream) {
                boolean isDirectory = Files.isDirectory(path);
                KeepFile file = new KeepFile();
                file.setName(path.getFileName().toString());
                file.setParent(parent);
                file.setDirectory(isDirectory);
                file.setCreationDateTime(getCreationDateTime(path));
                file.setModifiedDateTime(getModifiedDateTime(path));
                file.setSource(source);
                file.setSha256(calculateSha256(path));
                repository.save(file);
                if (isDirectory) {
                    read(source, path, file);
                }
            }
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

    private LocalDateTime getCreationDateTime(Path path) {
        try {
            return LocalDateTime.ofInstant(((FileTime) Files.getAttribute(path, "creationTime")).toInstant(), ZoneId.systemDefault());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    private LocalDateTime getModifiedDateTime(Path path) {
        try {
            return LocalDateTime.ofInstant((Files.getLastModifiedTime(path)).toInstant(), ZoneId.systemDefault());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }
}
