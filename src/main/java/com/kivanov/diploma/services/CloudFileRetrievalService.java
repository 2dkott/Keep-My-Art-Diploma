package com.kivanov.diploma.services;

import com.kivanov.diploma.model.KeepSource;

import java.io.IOException;

public interface CloudFileRetrievalService {

    void recordFileData(KeepSource source) throws IOException;
}
