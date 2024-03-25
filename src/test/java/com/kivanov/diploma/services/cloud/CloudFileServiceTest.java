package com.kivanov.diploma.services.cloud;

import com.kivanov.diploma.TestUtils;
import com.kivanov.diploma.model.KeepFile;
import com.kivanov.diploma.model.KeepSource;
import com.kivanov.diploma.model.SourceType;
import com.kivanov.diploma.services.CloudsFileService;
import com.kivanov.diploma.services.FileRepositoryService;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@EnableConfigurationProperties(UrlConfiguration.class)
public class CloudFileServiceTest {

    @Mock
    HttpRequestMaker httpRequestMaker;

    @MockBean
    FileRepositoryService fileRepositoryService;

    @Autowired
    UrlConfiguration urlConfiguration;

    @Autowired
    StandardPBEStringEncryptor encryptor;

    private final static String JSON_TEMPLATES_PATH = "json-templates/cloud/yandex/read-file-from-resource/";
    private final static String JSON_PARENT_TEMPLATE = "json-parent.json";
    private final static String JSON_CHILD_TEMPLATE = "json-child.json";

    static final String PARENT_URL = "parent";
    static final String PARENT_TEST_DIR_NAME = "ParentTestDirName";
    static final String PARENT_TEST_FILE_NAME = "ParentTestFileName";
    static final String CHILD_FILE_NAME = "ChildTestFileName";
    static final String CHILD_URL = PARENT_URL + "/" + PARENT_TEST_DIR_NAME;
    static final String TOKEN = "test_token";
    static final String PARENT_FILE_CREATION_TIME = "2021-03-06T22:07:27+00:00";
    static final String PARENT_FILE_MODIFIED_TIME = "2022-02-06T20:01:01+01:01";
    static final String PARENT_FILE_SHA_5 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
    static final String PARENT_DIR_CREATION_TIME = "2023-03-06T22:07:27+00:00";
    static final String PARENT_DIR_MODIFIED_TIME = "2011-02-06T20:01:01+01:01";
    static final String CHILD_CREATION_TIME = "2021-03-06T22:17:17+10:10";
    static final String CHILD_MODIFIED_TIME = "2022-02-11T10:11:11+11:11";
    static final String CHILD_FILE_SHA_5 = "e3b0c44298fc1c149afbf4c8996fb924adfasdf1e4649b934ca495991b7852b855";

    @BeforeEach
    public void beforeEach() throws IOException {
        String jsonParent = TestUtils.readFile(JSON_TEMPLATES_PATH + JSON_PARENT_TEMPLATE);
        String jsonChild = TestUtils.readFile(JSON_TEMPLATES_PATH + JSON_CHILD_TEMPLATE);

        jsonParent = jsonParent.replace("%parentDireName%", PARENT_TEST_DIR_NAME)
                .replace("%parentDirCreationTime%", PARENT_DIR_CREATION_TIME)
                .replace("%parentDirModifiedTime%", PARENT_DIR_MODIFIED_TIME)
                .replace("%parentFileName%", PARENT_TEST_FILE_NAME)
                .replace("%parentFileCreationTime%", PARENT_FILE_CREATION_TIME)
                .replace("%parentFileModifiedTime%", PARENT_FILE_MODIFIED_TIME)
                .replace("%parentFileSha5%", PARENT_FILE_SHA_5);
        jsonChild = jsonChild.replace("%childFileName%", CHILD_FILE_NAME)
                .replace("%childCreationTime%", CHILD_CREATION_TIME)
                .replace("%childModifiedTime%", CHILD_MODIFIED_TIME)
                .replace("%childFileSha5%", CHILD_FILE_SHA_5);


        when(httpRequestMaker.getGetResponseBody(urlConfiguration.getYandex().getRoot() + PARENT_URL, TOKEN)).thenReturn(jsonParent);
        when(httpRequestMaker.getGetResponseBody(urlConfiguration.getYandex().getRoot() + CHILD_URL, TOKEN)).thenReturn(jsonChild);
    }
    @Test
    public void testInitFindAndSaveAllFiles() {

        KeepSource yandexSource = new KeepSource();
        yandexSource.setType(SourceType.YANDEX);
        yandexSource.setPath(PARENT_URL);
        yandexSource.setUserToken(encryptor.encrypt(TOKEN));
        KeepFile rootKeeFile = KeepFile.Root(yandexSource);

        when(fileRepositoryService.saveRoot(yandexSource)).thenReturn(rootKeeFile);

        CloudsFileService cloudsFileService = new CloudsFileService(fileRepositoryService, httpRequestMaker, urlConfiguration, encryptor);
        cloudsFileService.initFindAndSaveAllFiles(yandexSource);

        ArgumentCaptor<KeepSource> keepSourceArgumentCaptor = ArgumentCaptor.forClass(KeepSource.class);
        ArgumentCaptor<KeepFile> rootKeepFileArgumentCaptor = ArgumentCaptor.forClass(KeepFile.class);
        ArgumentCaptor<List<KeepFile>> keepFileListArgumentCaptor = ArgumentCaptor.forClass(ArrayList.class);

        verify(fileRepositoryService, times(1)).saveRoot(keepSourceArgumentCaptor.capture());
        List<KeepSource> capturedKeepSource = keepSourceArgumentCaptor.getAllValues();
        assertEquals(capturedKeepSource.size(),1);
        assertEquals(capturedKeepSource.get(0), yandexSource);

        verify(fileRepositoryService, times(1)).saveKeepFileListByParent(rootKeepFileArgumentCaptor.capture(), keepFileListArgumentCaptor.capture());

        List<KeepFile> capturedKeepRoot = rootKeepFileArgumentCaptor.getAllValues();
        assertEquals(capturedKeepRoot.size(),1);
        assertEquals(capturedKeepRoot.get(0), rootKeeFile);

        List<KeepFile> capturedFiles = keepFileListArgumentCaptor.getAllValues().get(0);
        KeepFile parentFile = capturedFiles.stream().filter(file -> file.getName().equals(PARENT_TEST_FILE_NAME)).findFirst().orElseThrow();
        KeepFile parentDir = capturedFiles.stream().filter(file -> file.getName().equals(PARENT_TEST_DIR_NAME)).findFirst().orElseThrow();
        KeepFile childFile = capturedFiles.stream().filter(file -> file.getName().equals(CHILD_FILE_NAME)).findFirst().orElseThrow();
        Assertions.assertAll(
                () -> assertEquals(parentFile.getSource(), yandexSource),
                () -> assertFalse(parentFile.isDirectory()),
                () -> assertEquals(parentFile.getCreationDateTime(), LocalDateTime.parse(PARENT_FILE_CREATION_TIME, DateTimeFormatter.ISO_DATE_TIME)),
                () -> assertEquals(parentFile.getModifiedDateTime(), LocalDateTime.parse(PARENT_FILE_MODIFIED_TIME, DateTimeFormatter.ISO_DATE_TIME)),
                () -> assertEquals(parentFile.getParent(), rootKeeFile),
                () -> assertEquals(parentFile.getSha256(), PARENT_FILE_SHA_5),
                () -> assertFalse(parentFile.isRoot()),
                () -> assertFalse(parentFile.isDeleted()),

                () -> assertEquals(parentDir.getSource(), yandexSource),
                () -> assertTrue(parentDir.isDirectory()),
                () -> assertEquals(parentDir.getCreationDateTime(), LocalDateTime.parse(PARENT_DIR_CREATION_TIME, DateTimeFormatter.ISO_DATE_TIME)),
                () -> assertEquals(parentDir.getModifiedDateTime(), LocalDateTime.parse(PARENT_DIR_MODIFIED_TIME, DateTimeFormatter.ISO_DATE_TIME)),
                () -> assertEquals(parentDir.getParent(), rootKeeFile),
                () -> assertEquals(parentDir.getSha256(), null),
                () -> assertFalse(parentDir.isRoot()),
                () -> assertFalse(parentDir.isDeleted()),

                () -> assertEquals(childFile.getSource(), yandexSource),
                () -> assertFalse(childFile.isDirectory()),
                () -> assertEquals(childFile.getCreationDateTime(), LocalDateTime.parse(CHILD_CREATION_TIME, DateTimeFormatter.ISO_DATE_TIME)),
                () -> assertEquals(childFile.getModifiedDateTime(), LocalDateTime.parse(CHILD_MODIFIED_TIME, DateTimeFormatter.ISO_DATE_TIME)),
                () -> assertEquals(childFile.getParent(), parentDir),
                () -> assertEquals(childFile.getSha256(), CHILD_FILE_SHA_5),
                () -> assertFalse(childFile.isRoot()),
                () -> assertFalse(childFile.isDeleted())
        );
    }

    @Test
    public void testCollectKeepFilesByRootFile() {

        KeepSource yandexSource = new KeepSource();
        yandexSource.setType(SourceType.YANDEX);
        yandexSource.setPath(PARENT_URL);
        yandexSource.setUserToken(encryptor.encrypt(TOKEN));
        KeepFile rootKeeFile = KeepFile.Root(yandexSource);

        when(fileRepositoryService.saveRoot(yandexSource)).thenReturn(rootKeeFile);

        CloudsFileService cloudsFileService = new CloudsFileService(fileRepositoryService, httpRequestMaker, urlConfiguration, encryptor);
        List<KeepFile> capturedFiles =cloudsFileService.collectKeepFilesByRootFile(rootKeeFile, yandexSource);

        KeepFile parentFile = capturedFiles.stream().filter(file -> file.getName().equals(PARENT_TEST_FILE_NAME)).findFirst().orElseThrow();
        KeepFile parentDir = capturedFiles.stream().filter(file -> file.getName().equals(PARENT_TEST_DIR_NAME)).findFirst().orElseThrow();
        Assertions.assertAll(
                () -> assertEquals(parentFile.getSource(), yandexSource),
                () -> assertFalse(parentFile.isDirectory()),
                () -> assertEquals(parentFile.getCreationDateTime(), LocalDateTime.parse(PARENT_FILE_CREATION_TIME, DateTimeFormatter.ISO_DATE_TIME)),
                () -> assertEquals(parentFile.getModifiedDateTime(), LocalDateTime.parse(PARENT_FILE_MODIFIED_TIME, DateTimeFormatter.ISO_DATE_TIME)),
                () -> assertEquals(parentFile.getParent(), rootKeeFile),
                () -> assertEquals(parentFile.getSha256(), PARENT_FILE_SHA_5),
                () -> assertFalse(parentFile.isRoot()),
                () -> assertFalse(parentFile.isDeleted()),

                () -> assertEquals(parentDir.getSource(), yandexSource),
                () -> assertTrue(parentDir.isDirectory()),
                () -> assertEquals(parentDir.getCreationDateTime(), LocalDateTime.parse(PARENT_DIR_CREATION_TIME, DateTimeFormatter.ISO_DATE_TIME)),
                () -> assertEquals(parentDir.getModifiedDateTime(), LocalDateTime.parse(PARENT_DIR_MODIFIED_TIME, DateTimeFormatter.ISO_DATE_TIME)),
                () -> assertEquals(parentDir.getParent(), rootKeeFile),
                () -> assertEquals(parentDir.getSha256(), null),
                () -> assertFalse(parentDir.isRoot()),
                () -> assertFalse(parentDir.isDeleted())
                );
    }
}
