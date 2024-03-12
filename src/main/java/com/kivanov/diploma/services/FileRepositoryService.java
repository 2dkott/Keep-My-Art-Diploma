package com.kivanov.diploma.services;

import com.kivanov.diploma.model.KeepFile;
import com.kivanov.diploma.model.KeepProject;
import com.kivanov.diploma.model.KeepSource;
import com.kivanov.diploma.persistence.KeepFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FileRepositoryService {

    @Autowired
    KeepFileRepository fileRepository;

    public KeepFile saveRoot(KeepSource source){
        KeepFile rootKeepFile = new KeepFile();
        rootKeepFile.setName("");
        rootKeepFile.setRoot(true);
        rootKeepFile.setSource(source);
        fileRepository.save(rootKeepFile);
        return rootKeepFile;
    }

    public List<KeepFile> getProjectOnlyFiles(KeepProject project) {
        List<KeepFile> fileList = new ArrayList<>();
        fileList.addAll(fileRepository.findKeepFileBySource(project.getLocalSource()));
        fileList.addAll(fileRepository.findKeepFileBySource(project.getCloudSource()));
        return fileList;
    }

    public List<KeepFile> findALlFilesBYParent(KeepFile parent) {
        return fileRepository.findKeepFileByParent(parent);
    }

    public List<KeepFile> findNotDeletedFilesByParent(KeepFile parent) {
        return fileRepository.findKeepFileByParent(parent).stream().filter(keepFile -> !keepFile.isDeleted()).toList();
    }

    public KeepFile findRootOfSource(KeepSource source) {
        List<KeepFile> fileList = fileRepository.findKeepFilesByIsRootAndSource(true, source);
        if(fileList.isEmpty()) return null;
        else return fileList.get(0);
    }

    public void saveFile(KeepFile keepFile) {
        fileRepository.save(keepFile);
    }
}
