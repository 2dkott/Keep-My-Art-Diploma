package com.kivanov.diploma.services;

import com.kivanov.diploma.model.KeepFile;
import com.kivanov.diploma.model.KeepSource;
import com.kivanov.diploma.persistence.KeepFileRepository;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;

@AllArgsConstructor
public class LocalFileService implements FileService{

    private KeepFileRepository repository;

    @Override
    public void recordFiles(KeepSource source) throws IOException {
        KeepFile parent = new KeepFile();
        repository.save(parent);
        Path root = Paths.get(source.getPath());
        read(source, root, parent);
    }

    public void read(KeepSource source, Path rootPath, KeepFile parent) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(rootPath)) {
            for (Path path : stream) {
                FileTime creationTime = (FileTime) Files.getAttribute(path, "creationTime");
                LocalDateTime convertedFileTime = LocalDateTime.ofInstant(creationTime.toInstant(), ZoneId.systemDefault());
                boolean isDirectory = Files.isDirectory(path);
                KeepFile file = new KeepFile();
                file.setName(path.getFileName().toString());
                file.setParent(parent);
                file.setDirectory(isDirectory);
                file.setDeleted(false);
                file.setCreationTime(convertedFileTime);
                file.setSource(source);
                repository.save(file);
                if (isDirectory) {
                    read(source, path, file);
                }
            }
        }
    }
}
