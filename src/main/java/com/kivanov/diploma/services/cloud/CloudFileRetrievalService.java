package com.kivanov.diploma.services.cloud;

import com.kivanov.diploma.model.KeepFile;
import com.kivanov.diploma.model.KeepSource;

import java.io.IOException;
import java.util.List;

public interface CloudFileRetrievalService {

    List<KeepFile> fetchCLoudFileToKeepFiles(KeepFile keepFileRoot, KeepSource source, boolean flat) throws IOException;
}
