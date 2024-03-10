package com.kivanov.diploma.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

@Data
@Slf4j
public class KeepFileSync {

    public List<KeepFile> leftNotMatchedFileList = new ArrayList<>();
    public List<KeepFile> rightNotMatchedFileList = new ArrayList<>();
    public List<Pair<KeepFile, KeepFile>> modifiedFileList = new ArrayList<>();


    public void compareLeftToRightSource1(Function<KeepFile, List<KeepFile>> leftSourceProvider, Function<KeepFile, List<KeepFile>> rightSourceProvider, KeepFile leftRoot, KeepFile rightRoot, KeepSource source) {

        List<KeepFile> leftFileList = leftSourceProvider.apply(leftRoot);
        List<KeepFile> rightFileList = Objects.isNull(rightRoot.getId()) ? new ArrayList<>() : rightSourceProvider.apply(rightRoot);

        leftFileList.forEach(leftFile -> {
            Optional<KeepFile> rightFile = rightFileList.stream()
                    .filter(keepFile -> keepFile.getName().equals(leftFile.getName()) && keepFile.isDirectory()==leftFile.isDirectory()).findFirst();
            rightFile.ifPresentOrElse(keepFile -> {
                        if(!leftFile.isDirectory()) {
                            if(!keepFile.getSha256().equals(leftFile.getSha256())
                                    || !keepFile.getCreationDateTime().equals(leftFile.getCreationDateTime())
                                    || !keepFile.getModifiedDateTime().equals(leftFile.getModifiedDateTime())) {
                                keepFile.setCreationDateTime(leftFile.getCreationDateTime());
                                keepFile.setModifiedDateTime(leftFile.getModifiedDateTime());
                                keepFile.setSha256(leftFile.getSha256());
                                modifiedFileList.add(Pair.of(leftFile, keepFile));
                            }
                        } else compareLeftToRightSource(leftSourceProvider,
                                                        rightSourceProvider,
                                                        leftFile,
                                                        keepFile,
                                                        source);
                    },
                    () -> {
                        try {
                            KeepFile newKeepFile = leftFile.cloneNew();
                            newKeepFile.setParent(rightRoot);
                            newKeepFile.setSource(source);
                            leftNotMatchedFileList.add(newKeepFile);
                        } catch (CloneNotSupportedException e) {
                            log.error(e.getMessage());
                        }
                        if(leftFile.isDirectory()) {
                            compareLeftToRightSource(leftSourceProvider,
                                                     rightSourceProvider,
                                                     leftFile,
                                                     leftFile,
                                                     source);
                        }
                    });
        });

        rightFileList.forEach(rightFile -> {
            Optional<KeepFile> leftFile = rightFileList.stream()
                    .filter(keepFile -> keepFile.getName().equals(rightFile.getName()) && keepFile.isDirectory()==rightFile.isDirectory()).findFirst();
            leftFile.ifPresentOrElse(keepFile -> {
                        if(!rightFile.isDirectory()) {
                            if(!keepFile.getSha256().equals(rightFile.getSha256())
                                    || !keepFile.getCreationDateTime().equals(rightFile.getCreationDateTime())
                                    || !keepFile.getModifiedDateTime().equals(rightFile.getModifiedDateTime())) {
                                keepFile.setCreationDateTime(rightFile.getCreationDateTime());
                                keepFile.setModifiedDateTime(rightFile.getModifiedDateTime());
                                keepFile.setSha256(rightFile.getSha256());
                                if(modifiedFileList.stream().anyMatch(keepFileKeepFilePair -> keepFileKeepFilePair.getRight().equals(rightFile)))
                                modifiedFileList.add(Pair.of(rightFile, keepFile));
                            }
                        } else compareLeftToRightSource(leftSourceProvider,
                                rightSourceProvider,
                                rightFile,
                                keepFile,
                                source);
                    },
                    () -> {
                        try {
                            KeepFile newKeepFile = rightFile.cloneNew();
                            newKeepFile.setParent(rightRoot);
                            newKeepFile.setSource(source);
                            leftNotMatchedFileList.add(newKeepFile);
                        } catch (CloneNotSupportedException e) {
                            log.error(e.getMessage());
                        }
                        if(rightFile.isDirectory()) {
                            compareLeftToRightSource(leftSourceProvider,
                                    rightSourceProvider,
                                    rightFile,
                                    rightFile,
                                    source);
                        }
                    });
        });

        rightNotMatchedFileList.addAll(rightFileList.stream().filter(keepFile -> leftFileList.stream().noneMatch(syncFile1 -> syncFile1.getName().equals(keepFile.getName()))).toList());
        rightNotMatchedFileList.forEach(keepFile -> keepFile.setDeleted(true));
    }

    public void compareLeftToRightSource(Function<KeepFile, List<KeepFile>> leftSourceProvider, Function<KeepFile, List<KeepFile>> rightSourceProvider, KeepFile leftRoot, KeepFile rightRoot, KeepSource source) {

        List<KeepFile> leftFileList = leftSourceProvider.apply(leftRoot);
        List<KeepFile> rightFileList = Objects.isNull(rightRoot.getId()) ? new ArrayList<>() : rightSourceProvider.apply(rightRoot);
        log.info(leftFileList.toString());
        log.info(rightFileList.toString());

        leftFileList.forEach(leftFile -> {
            Optional<KeepFile> rightFile = rightFileList.stream()
                    .filter(keepFile -> keepFile.getName().equals(leftFile.getName()) && keepFile.isDirectory()==leftFile.isDirectory()).findFirst();
            rightFile.ifPresentOrElse(keepFile -> {
                        if(!leftFile.isDirectory()) {
                            if(!keepFile.getSha256().equals(leftFile.getSha256())
                                    || !keepFile.getCreationDateTime().equals(leftFile.getCreationDateTime())
                                    || !keepFile.getModifiedDateTime().equals(leftFile.getModifiedDateTime())) {
                                modifiedFileList.add(Pair.of(leftFile, keepFile));
                            }
                        } else compareLeftToRightSource(leftSourceProvider,
                                                        rightSourceProvider,
                                                        leftFile,
                                                        keepFile,
                                                        source);
                    },
                    () -> {
                        log.info("Unmatched Left File {}", leftFile);
                        leftNotMatchedFileList.add(leftFile);
                        if(leftFile.isDirectory()) {
                            log.info("UnmatchedLeft File {} is directory", leftFile);
                            compareLeftToRightSource(leftSourceProvider,
                                                     rightSourceProvider,
                                                     leftFile,
                                                     leftFile,
                                                     source);
                        }
                    });
        });
        rightFileList.forEach(rightFile -> {
            Optional<KeepFile> leftFile = leftFileList.stream()
                    .filter(keepFile -> keepFile.getName().equals(rightFile.getName()) && keepFile.isDirectory()==rightFile.isDirectory()).findFirst();
            if(leftFile.isEmpty()) {
                log.info("Unmatched Right File {}", rightFile);
                rightNotMatchedFileList.add(rightFile);
                if(rightFile.isDirectory()) {
                    log.info("Unmatched Right File {} is directory", rightFile);
                    compareLeftToRightSource(leftSourceProvider,
                            rightSourceProvider,
                            rightFile,
                            rightFile,
                            source);
                }
            }
        });
        System.out.println();
    }
}
