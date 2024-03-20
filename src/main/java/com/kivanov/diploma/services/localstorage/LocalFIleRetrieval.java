package com.kivanov.diploma.services.localstorage;

import com.google.common.collect.Lists;
import com.kivanov.diploma.model.KeepFile;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class LocalFIleRetrieval {

    public static void searchAndMapFilesToKeepFileList(Path rootPath, KeepFile parent, List<KeepFile> keepFilesStorage) throws LocalFileReadingException {
        List<LocalPathFile> localPathFiles = collectAndMapChildOfPath(rootPath);
        localPathFiles.forEach(localPathFile -> {
            KeepFile keepFile = localPathFile.mapToKeepFile(parent);
            keepFilesStorage.add(keepFile);
            if(localPathFile.isDirectory()) {
                try {
                    searchAndMapFilesToKeepFileList(localPathFile.getPath(), keepFile, keepFilesStorage);
                } catch (LocalFileReadingException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public static void flatSearchAndMapFilesToKeepFileList(Path rootPath, KeepFile parent, List<KeepFile> keepFilesStorage) throws LocalFileReadingException {
        List<LocalPathFile> localPathFiles = collectAndMapChildOfPath(rootPath);
        localPathFiles.forEach(localPathFile -> {
            KeepFile keepFile = localPathFile.mapToKeepFile(parent);
            keepFilesStorage.add(keepFile);
        });
    }

    private static List<LocalPathFile> collectAndMapChildOfPath(Path rootPath) throws LocalFileReadingException {
        log.info("Reading Path Files from root Path {}", rootPath);
        try {
            if(Files.exists(rootPath)) return Lists.newArrayList(Files.newDirectoryStream(rootPath)).stream().map(LocalPathFile::new).toList();
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Reading Path Files from root Path {} was failed", rootPath);
            log.error("");
            throw new LocalFileReadingException(e.getMessage());
        }
    }

}
