package com.kivanov.diploma.services;

import com.kivanov.diploma.model.KeepFile;
import com.kivanov.diploma.model.KeepSource;

import java.io.IOException;
import java.util.List;

public interface FileService {

    void initRecordFiles(KeepSource source) throws IOException;

    //List<KeepFile> collectKeepFilesByRootFile(KeepFile rootPath, KeepSource keepSource, List<KeepFile> fileList) throws IOException;
    List<KeepFile> collectKeepFilesByRootFile(KeepFile rootPath, KeepSource keepSource);
}
