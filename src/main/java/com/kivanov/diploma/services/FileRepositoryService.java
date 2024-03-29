package com.kivanov.diploma.services;

import com.kivanov.diploma.model.KeepFile;
import com.kivanov.diploma.model.KeepProject;
import com.kivanov.diploma.model.KeepSource;
import com.kivanov.diploma.persistence.KeepFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FileRepositoryService {

    @Autowired
    KeepFileRepository fileRepository;

    public KeepFile saveRoot(KeepSource source){
        KeepFile rootKeepFile = new KeepFile();
        rootKeepFile.setName("");
        rootKeepFile.setRoot(true);
        rootKeepFile.setSource(source);
        saveFile(rootKeepFile);
        return rootKeepFile;
    }

    public List<KeepFile> getProjectOnlyFiles(KeepProject project) {
        List<KeepFile> fileList = new ArrayList<>();
        fileList.addAll(fileRepository.findKeepFileBySource(project.getLocalSource()));
        fileList.addAll(fileRepository.findKeepFileBySource(project.getCloudSource()));
        return fileList;
    }

    public void saveKeepFileListByParent(KeepFile rootKeepFile, List<KeepFile> keepFiles) {
        saveWithChildren(rootKeepFile, keepFiles);
    }

    private void saveWithChildren(KeepFile keepFileRoot, List<KeepFile> fileList) {
        saveFile(keepFileRoot);
        List<KeepFile> matchedFileList = fileList.stream().filter(keepFile -> keepFile.getParent().equals(keepFileRoot)).toList();
        fileList.removeAll(matchedFileList);
        matchedFileList.forEach(keepFile -> {
            saveWithChildren(keepFile, fileList);
        });
    }

    public List<KeepFile> findALlFilesBySource(KeepSource source) {
        return fileRepository.findKeepFilesBySource(source);
    }

    public Optional<KeepFile> findFileByPathIdAndSource(KeepFile keepFile, KeepSource source) {
        List<KeepFile> result = fileRepository.findKeepFilesByPathIdAndSource(keepFile.getPathId(), source);
        return result.stream().filter(keepFile1 -> !keepFile1.isDeleted()).findFirst();
    }

    public List<KeepFile> findNotDeletedFilesByParent(KeepFile parent) {
        return fileRepository.findKeepFileByParent(parent).stream().filter(keepFile -> !keepFile.isDeleted()).toList();
    }

    public List<KeepFile> findNotDeletedFilesByParentAndSource(KeepFile parent, KeepSource source) {
        return fileRepository.findKeepFilesByParentAndSource(parent, source).stream().filter(keepFile -> !keepFile.isDeleted()).toList();
    }

    public Optional<KeepFile> findRootOfSource(KeepSource source) {
        List<KeepFile> fileList = fileRepository.findKeepFilesByIsRootAndSource(true, source);
        return Optional.ofNullable(fileList.isEmpty() ? null : fileList.get(0));
    }

    public void saveFile(KeepFile keepFile) {
        fileRepository.save(keepFile);
    }
}
