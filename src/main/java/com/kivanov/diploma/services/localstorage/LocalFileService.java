package com.kivanov.diploma.services.localstorage;

import com.kivanov.diploma.model.KeepFile;
import com.kivanov.diploma.model.KeepSource;
import com.kivanov.diploma.services.FileRepositoryService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@AllArgsConstructor
public class LocalFileService {

    private FileRepositoryService fileRepositoryService;


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

    public List<KeepFile> collectKeepFilesByRootFile(KeepFile keepFile, KeepSource source) throws LocalFileReadingException {
        log.info("Initiate getting data from Local Storage {}", source.getPath() + keepFile.getPathId());
        Path localPath = Paths.get(source.getPath() + keepFile.getPathId()) ;
        List<KeepFile> keepFileList = new ArrayList<>();
        LocalFIleRetrieval.flatSearchAndMapFilesToKeepFileList(localPath, keepFile, keepFileList);
        log.info("File List was fetched {}", keepFileList);
        return keepFileList;
    }

    public void checkAndCreateDirectories(List<KeepFile> keepFiles, KeepSource localKeePSource) {
        keepFiles.forEach(keepFile -> createDirectoryIfNeeded(keepFile.getParent(), localKeePSource));
    }

    private void createDirectoryIfNeeded(KeepFile keepFile, KeepSource localKeePSource) {
        Optional<KeepFile> cloudDir = fileRepositoryService.findFileByPathIdAndSource(keepFile, localKeePSource);
        if(cloudDir.isEmpty()) {
            createDirectoryIfNeeded(keepFile.getParent(), localKeePSource);
            File theDir = new File(localKeePSource.getPath() + keepFile.getPathId());
            if (!theDir.exists()){
                theDir.mkdirs();
            }
        }
    }
}
