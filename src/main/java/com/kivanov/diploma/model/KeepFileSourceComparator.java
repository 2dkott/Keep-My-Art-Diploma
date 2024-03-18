package com.kivanov.diploma.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Data
@Slf4j
public class KeepFileSourceComparator {

    public List<KeepFile> leftNotMatchedFileList = new ArrayList<>();
    public List<KeepFile> rightNotMatchedFileList = new ArrayList<>();
    public List<Pair<KeepFile, KeepFile>> modifiedFileList = new ArrayList<>();

    public void compareLeftToRightSource(Function<KeepFile, List<KeepFile>> leftSourceProvider, Function<KeepFile, List<KeepFile>> rightSourceProvider, KeepFile leftRoot, KeepFile rightRoot, KeepSource source) {

        List<KeepFile> leftFileList = leftSourceProvider.apply(leftRoot);
        List<KeepFile> rightFileList = rightSourceProvider.apply(rightRoot);

        log.info("Left Parent File is {}", leftRoot);
        log.info("Here is Left List: {}", leftFileList);
        log.info("Right Parent File is {}", rightRoot);
        log.info("Here is Right List: {}", rightFileList);

        leftFileList.forEach(leftFile -> {
            log.info("Step to Left File {}", leftFile);
            Optional<KeepFile> rightFile = rightFileList.stream()
                    .filter(keepFile -> keepFile.getName().equals(leftFile.getName()) && keepFile.isDirectory()==leftFile.isDirectory()).findFirst();
            rightFile.ifPresentOrElse(keepFile -> {
                        log.info("Right File {} was found", keepFile);
                        if(!leftFile.isDirectory()) {
                            if(!keepFile.getSha256().equals(leftFile.getSha256())
                                    || !keepFile.getCreationDateTime().equals(leftFile.getCreationDateTime())
                                    || !keepFile.getModifiedDateTime().equals(leftFile.getModifiedDateTime())) {
                                log.info("Left and Right File are different");
                                log.info("Left Sha256 and Right Sha256: {} - {}", leftFile.getSha256(), keepFile.getSha256() );
                                log.info("Left CreationDateTime and Right CreationDateTime: {} - {}", leftFile.getCreationDateTime(), keepFile.getCreationDateTime());
                                log.info("Left ModifiedDateTime and Right ModifiedDateTime: {} - {}", leftFile.getModifiedDateTime(), keepFile.getModifiedDateTime());
                                log.info("Putting Left and Right File to ModifiedFileList: {} - {}", leftFile, keepFile);
                                modifiedFileList.add(Pair.of(leftFile, keepFile));
                                log.info("ModifiedFileList is : {}", modifiedFileList);
                            }
                        } else {
                                log.info("Left File {} is directory", leftFile);
                                log.info("Right File {} was found", leftFile);
                                log.info("Go in Right File {} Directory", leftFile);
                                compareLeftToRightSource(leftSourceProvider,
                                        rightSourceProvider,
                                        leftFile,
                                        keepFile,
                                        source);
                        }
                    },
                    () -> {
                        log.info("There is no Right File same as Left File {}", leftFile);
                        log.info("Putting Left File {} to leftNotMatchedFileList", leftFile);
                        leftNotMatchedFileList.add(leftFile);
                        log.info("ModifiedFileList is : {}", leftNotMatchedFileList);
                        if(leftFile.isDirectory()) {
                            log.info("Left File {} is directory", leftFile);
                            log.info("Go in Left File {} Directory", leftFile);
                            compareLeftToRightSource(leftSourceProvider,
                                                     rightSourceProvider,
                                                     leftFile,
                                                     leftFile,
                                                     source);
                        }
                    });
        });
        log.info("Start Match Right Files with Left Files");
        rightFileList.forEach(rightFile -> {
            log.info("Step to Right File {}", rightFile);
            Optional<KeepFile> leftFile = leftFileList.stream()
                    .filter(keepFile -> keepFile.getName().equals(rightFile.getName()) && keepFile.isDirectory()==rightFile.isDirectory()).findFirst();
            if(leftFile.isEmpty()) {
                log.info("There is no Left File same as Right File {}", rightFile);
                log.info("Putting Right File {} to rightNotMatchedFileList", rightFile);
                rightNotMatchedFileList.add(rightFile);
                log.info("rightNotMatchedFileList is : {}", rightNotMatchedFileList);
                if(rightFile.isDirectory()) {
                    log.info("Right File {} is directory", rightFile);
                    log.info("Go in Right File {} Directory", rightFile);
                    compareLeftToRightSource(leftSourceProvider,
                                             rightSourceProvider,
                                             rightFile,
                                             rightFile,
                                             source);
                }
            }
        });
    }
}
