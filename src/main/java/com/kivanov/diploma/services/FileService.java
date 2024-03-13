package com.kivanov.diploma.services;

import com.kivanov.diploma.model.KeepFile;
import com.kivanov.diploma.model.KeepSource;

import java.io.IOException;
import java.util.List;

public interface FileService {

    void initFindAndSaveAllFiles(KeepSource source);

    List<KeepFile> collectKeepFilesByRootFile(KeepFile rootPath, KeepSource keepSource);
}
