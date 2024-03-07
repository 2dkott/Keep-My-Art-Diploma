package com.kivanov.diploma.services;

import com.kivanov.diploma.model.KeepFile;
import com.kivanov.diploma.model.KeepProject;
import com.kivanov.diploma.persistence.KeepFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FileHandler {

    @Autowired
    KeepFileRepository fileRepository;

    public List<KeepFile> getProjectOnlyFiles(KeepProject project) {
        List<KeepFile> fileList = new ArrayList<>();
        fileList.addAll(fileRepository.findKeepFileBySource(project.getLocalSource()));
        fileList.addAll(fileRepository.findKeepFileBySource(project.getCloudSource()));
        return fileList;
    }
}
