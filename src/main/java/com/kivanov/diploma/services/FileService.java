package com.kivanov.diploma.services;

import com.kivanov.diploma.model.KeepFile;
import com.kivanov.diploma.model.KeepProject;
import com.kivanov.diploma.model.KeepSource;
import com.kivanov.diploma.persistence.KeepFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Component
public class FileService {

    @Autowired
    KeepFileRepository repository;


    public void recordFiles(KeepSource source, String rootPath) throws IOException {
        KeepFile parent = new KeepFile();
        repository.save(parent);
        Path root = Paths.get(rootPath);
        read(source, root, parent);
    }

    public List<KeepFile> getProjectFiles(KeepProject project) {
        List<KeepFile> fileList = new ArrayList<>();
        fileList.addAll(repository.findKeepFileBySource(project.getLocalSource()));
        fileList.addAll(repository.findKeepFileBySource(project.getCloudSource()));
        return fileList;
    }

    public List<KeepFile> getProjectOnlyFiles(KeepProject project) {
        List<KeepFile> fileList = new ArrayList<>();
        fileList.addAll(repository.findKeepFileBySource(project.getLocalSource()));
        fileList.addAll(repository.findKeepFileBySource(project.getCloudSource()));
        return fileList;
    }

    public void read(KeepSource source, Path rootPath, KeepFile parent) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(rootPath)) {
            for (Path path : stream) {
                System.out.println(path.getFileName().toString());
                System.out.println(path.getFileName().toString());
                FileTime creationTime = (FileTime) Files.getAttribute(path, "creationTime");
                LocalDateTime convertedFileTime = LocalDateTime.ofInstant(creationTime.toInstant(), ZoneId.systemDefault());
                boolean isDirectory = Files.isDirectory(path);
                System.out.println(creationTime);
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
