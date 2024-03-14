package com.kivanov.diploma.services.localstorage;

import com.kivanov.diploma.model.KeepFile;
import com.kivanov.diploma.model.KeepSource;
import com.kivanov.diploma.services.FileRepositoryService;
import com.kivanov.diploma.services.FileService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@AllArgsConstructor
public class LocalFileService implements FileService {

    private FileRepositoryService fileRepositoryService;

    @Override
    public void initFindAndSaveAllFiles(KeepSource source) throws LocalFileReadingException {
        log.info("Initiate getting data from Local Storage {}", source.getPath());
        KeepFile rootKeepFile = fileRepositoryService.saveRoot(source);
        log.info("Root File id:{} is saved", rootKeepFile.getId());
        Path localPath = Paths.get(source.getPath()) ;
        List<KeepFile> keepFileList = new ArrayList<>();
        LocalFIleRetrieval.searchAndMapFilesToKeepFileList(localPath, rootKeepFile, keepFileList);
        log.info("File List was fetched {}", keepFileList);
        fileRepositoryService.saveKeepFileListByParent(rootKeepFile, keepFileList);
        log.info("File List was saved in DB");
    }

    @Override
    public List<KeepFile> collectKeepFilesByRootFile(KeepFile keepFile, KeepSource source) throws LocalFileReadingException {
        log.info("Initiate getting data from Local Storage {}", source.getPath() + keepFile.getPathId());
        Path localPath = Paths.get(source.getPath() + keepFile.getPathId()) ;
        List<KeepFile> keepFileList = new ArrayList<>();
        LocalFIleRetrieval.flatSearchAndMapFilesToKeepFileList(localPath, keepFile, keepFileList);
        log.info("File List was fetched {}", keepFileList);
        return keepFileList;
    }

}
