package com.kivanov.diploma.services.repository;

import com.kivanov.diploma.model.KeepFile;
import com.kivanov.diploma.model.KeepSource;
import com.kivanov.diploma.services.FileRepositoryService;
import com.kivanov.diploma.services.SourceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class FileRepositoryServiceTest {

    @Autowired
    FileRepositoryService fileRepositoryService;

    @Autowired
    SourceService sourceService;


    @Test
    public void testSaveFileListByParent() {
        KeepSource keepSource = new KeepSource();
        sourceService.saveKeepSource(keepSource);

        KeepFile keepFileRoot = fileRepositoryService.saveRoot(keepSource);

        KeepFile keepFileA = new KeepFile();
        keepFileA.setParent(keepFileRoot);
        keepFileA.setSource(keepSource);
        keepFileA.setName("keepFileA");

        KeepFile keepFileAA = new KeepFile();
        keepFileAA.setParent(keepFileRoot);
        keepFileAA.setSource(keepSource);
        keepFileAA.setName("keepFileAA");

        KeepFile keepFileB = new KeepFile();
        keepFileB.setParent(keepFileRoot);
        keepFileB.setSource(keepSource);
        keepFileB.setName("keepFileB");

        KeepFile keepFileBA = new KeepFile();
        keepFileBA.setParent(keepFileB);
        keepFileBA.setSource(keepSource);
        keepFileBA.setName("keepFileBA");

        KeepFile keepFileBAA = new KeepFile();
        keepFileBAA.setParent(keepFileB);
        keepFileBAA.setSource(keepSource);
        keepFileBAA.setName("keepFileBAA");

        KeepFile keepFileBB = new KeepFile();
        keepFileBB.setParent(keepFileB);
        keepFileBB.setSource(keepSource);
        keepFileBB.setName("keepFileBB");

        KeepFile keepFileBBA = new KeepFile();
        keepFileBBA.setParent(keepFileBB);
        keepFileBBA.setSource(keepSource);
        keepFileBBA.setName("keepFileBBA");

        List<KeepFile> fileList = new ArrayList<>();
        fileList.add(keepFileA);
        fileList.add(keepFileAA);
        fileList.add(keepFileB);
        fileList.add(keepFileBA);
        fileList.add(keepFileBAA);
        fileList.add(keepFileBB);
        fileList.add(keepFileBBA);

        fileRepositoryService.saveKeepFileListByParent(keepFileRoot, fileList);
        List<KeepFile> actualList = fileRepositoryService.findALlFilesBySource(keepSource);
        assertAll(
                () -> assertEquals(actualList.size(), 8),
                () -> assertEquals(actualList.stream().filter(keepFile -> keepFile.getName().equals("keepFileA")).findFirst().orElseThrow().getParent().getPathId(), keepFileRoot.getPathId()),
                () -> assertEquals(actualList.stream().filter(keepFile -> keepFile.getName().equals("keepFileAA")).findFirst().orElseThrow().getParent().getPathId(), keepFileRoot.getPathId()),
                () -> assertEquals(actualList.stream().filter(keepFile -> keepFile.getName().equals("keepFileB")).findFirst().orElseThrow().getParent().getPathId(), keepFileRoot.getPathId()),
                () -> assertEquals(actualList.stream().filter(keepFile -> keepFile.getName().equals("keepFileBA")).findFirst().orElseThrow().getParent().getPathId(), keepFileB.getPathId()),
                () -> assertEquals(actualList.stream().filter(keepFile -> keepFile.getName().equals("keepFileBAA")).findFirst().orElseThrow().getParent().getPathId(), keepFileB.getPathId()),
                () -> assertEquals(actualList.stream().filter(keepFile -> keepFile.getName().equals("keepFileBB")).findFirst().orElseThrow().getParent().getPathId(), keepFileB.getPathId()),
                () -> assertEquals(actualList.stream().filter(keepFile -> keepFile.getName().equals("keepFileBBA")).findFirst().orElseThrow().getParent().getPathId(), keepFileBB.getPathId())
        );
    }

    @Test
    public void tesEmptyListWithSaveFileListByParent() {
        KeepSource keepSource = new KeepSource();
        sourceService.saveKeepSource(keepSource);

        KeepFile keepFileRoot = fileRepositoryService.saveRoot(keepSource);
        List<KeepFile> fileList = new ArrayList<>();
        fileRepositoryService.saveKeepFileListByParent(keepFileRoot, fileList);
        List<KeepFile> actualList = fileRepositoryService.findALlFilesBySource(keepSource);
        assertEquals(actualList.size(), 1);
    }

}

