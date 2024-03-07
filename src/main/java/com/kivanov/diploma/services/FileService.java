package com.kivanov.diploma.services;

import com.kivanov.diploma.model.KeepSource;

import java.io.IOException;

public interface FileService {

    void recordFiles(KeepSource source) throws IOException;
}
