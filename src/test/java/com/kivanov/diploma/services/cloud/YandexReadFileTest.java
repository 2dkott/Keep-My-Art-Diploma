package com.kivanov.diploma.services.cloud;

import com.kivanov.diploma.TestUtils;
import com.kivanov.diploma.model.KeepFile;
import com.kivanov.diploma.model.KeepSource;
import com.kivanov.diploma.model.SourceType;
import com.kivanov.diploma.services.FileRepositoryService;
import com.kivanov.diploma.services.cloud.yandex.YandexFileRetrievalService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@EnableConfigurationProperties(UrlConfiguration.class)
public class YandexReadFileTest {

    @Value("${urls.sources.yandex.root}")
    private String resourcesUrl;

    @Mock
    HttpRequestMaker httpRequestMaker;

    @MockBean
    FileRepositoryService fileRepositoryService;

    @Autowired
    UrlConfiguration urlConfiguration;

    private final static String JSON_TEMPLATES_PATH = "json-templates/cloud/yandex/read-file-from-resource/";
    private final static String JSON_PARENT_TEMPLATE = "json-parent.json";
    private final static String JSON_CHILD_TEMPLATE = "json-child.json";

    @Test
    public void testReadFiles() throws IOException {
        String jsonParent = TestUtils.readFile(JSON_TEMPLATES_PATH + JSON_PARENT_TEMPLATE);
        String jsonChild = TestUtils.readFile(JSON_TEMPLATES_PATH + JSON_CHILD_TEMPLATE);

        String parentFileName = "ParentTestFileName";
        String parentFileCreationTime = "2021-03-06T22:07:27+00:00";
        String parentFileModifiedTime = "2022-02-06T20:01:01+01:01";
        String parentFileSha5 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

        String parentDireName = "ParentTestDirName";
        String parentDirCreationTime = "2023-03-06T22:07:27+00:00";
        String parentDirModifiedTime = "2011-02-06T20:01:01+01:01";

        String childFileName = "ChildTestFileName";
        String childCreationTime = "2021-03-06T22:17:17+10:10";
        String childModifiedTime = "2022-02-11T10:11:11+11:11";
        String childFileSha5 = "e3b0c44298fc1c149afbf4c8996fb924adfasdf1e4649b934ca495991b7852b855";

        jsonParent = jsonParent.replace("%parentDireName%", parentDireName)
                .replace("%parentDirCreationTime%", parentDirCreationTime)
                .replace("%parentDirModifiedTime%", parentDirModifiedTime)
                .replace("%parentFileName%", parentFileName)
                .replace("%parentFileCreationTime%", parentFileCreationTime)
                .replace("%parentFileModifiedTime%", parentFileModifiedTime)
                .replace("%parentFileSha5%", parentFileSha5);
        jsonChild = jsonChild.replace("%childFileName%", childFileName)
                .replace("%childCreationTime%", childCreationTime)
                .replace("%childModifiedTime%", childModifiedTime)
                .replace("%childFileSha5%", childFileSha5);

        String parentUrl = "parent";
        String childUrl = parentUrl + "/" + parentDireName;
        String token = "test_token";


        when(httpRequestMaker.getResponse(urlConfiguration.getYandex().getRoot() + parentUrl, token)).thenReturn(jsonParent);
        when(httpRequestMaker.getResponse(urlConfiguration.getYandex().getRoot() + childUrl, token)).thenReturn(jsonChild);

        KeepSource yandexSource = new KeepSource();
        yandexSource.setType(SourceType.YANDEX);
        yandexSource.setPath(parentUrl);
        yandexSource.setUserToken(token);

        YandexFileRetrievalService fileRetrievalService = new YandexFileRetrievalService(fileRepositoryService, httpRequestMaker, urlConfiguration);

        fileRetrievalService.recordFileData(yandexSource);

        ArgumentCaptor<KeepFile> fileArgumentCaptor = ArgumentCaptor.forClass(KeepFile.class);
        verify(fileRepositoryService, times(3)).saveFile(fileArgumentCaptor.capture());
        List<KeepFile> capturedFiles = fileArgumentCaptor.getAllValues();
        KeepFile parentFile = capturedFiles.stream().filter(file -> file.getName().equals(parentFileName)).findFirst().orElseThrow();
        KeepFile parentDir = capturedFiles.stream().filter(file -> file.getName().equals(parentDireName)).findFirst().orElseThrow();
        KeepFile childFile = capturedFiles.stream().filter(file -> file.getName().equals(childFileName)).findFirst().orElseThrow();
        Assertions.assertAll(
                () -> assertEquals(parentFile.getSource(), yandexSource),
                () -> assertFalse(parentFile.isDirectory()),
                () -> assertEquals(parentFile.getCreationDateTime(), LocalDateTime.parse(parentFileCreationTime, DateTimeFormatter.ISO_DATE_TIME)),
                () -> assertEquals(parentFile.getModifiedDateTime(), LocalDateTime.parse(parentFileModifiedTime, DateTimeFormatter.ISO_DATE_TIME)),

                () -> assertEquals(parentDir.getSource(), yandexSource),
                () -> assertTrue(parentDir.isDirectory()),
                () -> assertEquals(parentDir.getCreationDateTime(), LocalDateTime.parse(parentDirCreationTime, DateTimeFormatter.ISO_DATE_TIME)),
                () -> assertEquals(parentDir.getModifiedDateTime(), LocalDateTime.parse(parentDirModifiedTime, DateTimeFormatter.ISO_DATE_TIME)),

                () -> assertEquals(childFile.getSource(), yandexSource),
                () -> assertFalse(childFile.isDirectory()),
                () -> assertEquals(childFile.getCreationDateTime(), LocalDateTime.parse(childCreationTime, DateTimeFormatter.ISO_DATE_TIME)),
                () -> assertEquals(childFile.getModifiedDateTime(), LocalDateTime.parse(childModifiedTime, DateTimeFormatter.ISO_DATE_TIME)),
                () -> assertEquals(childFile.getParent(), parentDir)
        );
    }

}
