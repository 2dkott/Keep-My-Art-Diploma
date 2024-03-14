package com.kivanov.diploma.services;

import com.kivanov.diploma.model.KeepFile;
import com.kivanov.diploma.model.KeepSource;
import com.kivanov.diploma.services.localstorage.LocalFileReadingException;

import java.util.List;

public interface FileService {

    void initFindAndSaveAllFiles(KeepSource source) throws FileDealingException;

    List<KeepFile> collectKeepFilesByRootFile(KeepFile rootPath, KeepSource keepSource) throws FileDealingException;
}
