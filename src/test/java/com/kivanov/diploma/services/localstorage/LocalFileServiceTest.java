package com.kivanov.diploma.services.localstorage;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;
import com.google.common.io.Resources;
import com.kivanov.diploma.model.KeepFile;
import com.kivanov.diploma.model.KeepSource;
import com.kivanov.diploma.model.SourceType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;


@Slf4j
@SpringBootTest
public class LocalFileServiceTest {

    private static Path rootFile;
    private static Path rootDirChildFIle;
    private static Path rootParentDir;


    static final String TEST_PATH = "src/test/resources/localstorage/";
    static final String ROOT_FILE = "rootFile.txt";
    static final String CHILD_FILE = "childFile.txt";
    @TempDir(cleanup=CleanupMode.ALWAYS)
    static Path rootDir;

    static final String PARENT_TEST_DIR_NAME = "ParentTestDirName";
    static final String PARENT_TEST_FILE_NAME = "ParentTestFileName.txt";
    static final String CHILD_FILE_NAME = "ChildTestFileName.txt";
    static final String CHILD_URL = PARENT_TEST_DIR_NAME + "/" + CHILD_FILE_NAME;

    @BeforeAll
    public static void beforeAll() throws IOException {
        Path rootFileOriginalPath = Paths.get(TEST_PATH, ROOT_FILE);
        rootFile = Files.copy(rootFileOriginalPath, rootDir.resolve(PARENT_TEST_FILE_NAME), StandardCopyOption.REPLACE_EXISTING);
        rootParentDir = Files.createDirectory(rootDir.resolve(PARENT_TEST_DIR_NAME));
        Path childFileOriginalPath = Paths.get(TEST_PATH, CHILD_FILE);
        rootDirChildFIle = Files.copy(childFileOriginalPath, rootDir.resolve(CHILD_URL), StandardCopyOption.REPLACE_EXISTING);
    }
    @Test
    public void testSearchAndMapFilesToKeepFileList() throws LocalFileReadingException {
        KeepSource localSource = new KeepSource();
        localSource.setType(SourceType.LOCAL);
        KeepFile rootKeepFile = KeepFile.Root(localSource);
        List<KeepFile> keepFilesStorage = new ArrayList<>();
        LocalFIleRetrieval.searchAndMapFilesToKeepFileList(rootDir, rootKeepFile, keepFilesStorage);
        assertEquals(keepFilesStorage.size(), 3);
        KeepFile parentFile = keepFilesStorage.stream().filter(file -> file.getName().equals(PARENT_TEST_FILE_NAME)).findFirst().orElseThrow();
        KeepFile parentDir = keepFilesStorage.stream().filter(file -> file.getName().equals(PARENT_TEST_DIR_NAME)).findFirst().orElseThrow();
        KeepFile childFile = keepFilesStorage.stream().filter(file -> file.getName().equals(CHILD_FILE_NAME)).findFirst().orElseThrow();
        Assertions.assertAll(
                () -> assertEquals(parentFile.getSource(), localSource),
                () -> assertFalse(parentFile.isDirectory()),
                () -> assertEquals(parentFile.getCreationDateTime(), getCreatedDateTime(rootFile).truncatedTo(ChronoUnit.SECONDS)),
                () -> assertEquals(parentFile.getModifiedDateTime(), getModifiedDateTime(rootFile).truncatedTo(ChronoUnit.SECONDS)),
                () -> assertEquals(parentFile.getParent(), rootKeepFile),
                () -> assertEquals(parentFile.getSha256(), getSha256(rootFile)),
                () -> assertFalse(parentFile.isRoot()),
                () -> assertFalse(parentFile.isDeleted()),

                () -> assertEquals(parentDir.getSource(), localSource),
                () -> assertTrue(parentDir.isDirectory()),
                () -> assertEquals(parentDir.getCreationDateTime(), getCreatedDateTime(rootParentDir).truncatedTo(ChronoUnit.SECONDS)),
                () -> assertEquals(parentDir.getModifiedDateTime(), getModifiedDateTime(rootParentDir).truncatedTo(ChronoUnit.SECONDS)),
                () -> assertEquals(parentDir.getParent(), rootKeepFile),
                () -> assertEquals(parentDir.getSha256(), null),
                () -> assertFalse(parentDir.isRoot()),
                () -> assertFalse(parentDir.isDeleted()),

                () -> assertEquals(childFile.getSource(), localSource),
                () -> assertFalse(childFile.isDirectory()),
                () -> assertEquals(childFile.getCreationDateTime(), getCreatedDateTime(rootDirChildFIle).truncatedTo(ChronoUnit.SECONDS)),
                () -> assertEquals(childFile.getModifiedDateTime(), getModifiedDateTime(rootDirChildFIle).truncatedTo(ChronoUnit.SECONDS)),
                () -> assertEquals(childFile.getParent(), parentDir),
                () -> assertEquals(childFile.getSha256(), getSha256(rootDirChildFIle)),
                () -> assertFalse(childFile.isRoot()),
                () -> assertFalse(childFile.isDeleted())
        );
    }

    @Test
    public void testFlatSearchAndMapFilesToKeepFileList() throws LocalFileReadingException {
        KeepSource localSource = new KeepSource();
        localSource.setType(SourceType.LOCAL);
        KeepFile rootKeepFile = KeepFile.Root(localSource);
        List<KeepFile> keepFilesStorage = new ArrayList<>();
        LocalFIleRetrieval.flatSearchAndMapFilesToKeepFileList(rootDir, rootKeepFile, keepFilesStorage);
        assertEquals(keepFilesStorage.size(), 2);
        KeepFile parentFile = keepFilesStorage.stream().filter(file -> file.getName().equals(PARENT_TEST_FILE_NAME)).findFirst().orElseThrow();
        KeepFile parentDir = keepFilesStorage.stream().filter(file -> file.getName().equals(PARENT_TEST_DIR_NAME)).findFirst().orElseThrow();
        Assertions.assertAll(
                () -> assertEquals(parentFile.getSource(), localSource),
                () -> assertFalse(parentFile.isDirectory()),
                () -> assertEquals(parentFile.getCreationDateTime(), getCreatedDateTime(rootFile).truncatedTo(ChronoUnit.SECONDS)),
                () -> assertEquals(parentFile.getModifiedDateTime(), getModifiedDateTime(rootFile).truncatedTo(ChronoUnit.SECONDS)),
                () -> assertEquals(parentFile.getParent(), rootKeepFile),
                () -> assertEquals(parentFile.getSha256(), getSha256(rootFile)),
                () -> assertFalse(parentFile.isRoot()),
                () -> assertFalse(parentFile.isDeleted()),

                () -> assertEquals(parentDir.getSource(), localSource),
                () -> assertTrue(parentDir.isDirectory()),
                () -> assertEquals(parentDir.getCreationDateTime(), getCreatedDateTime(rootParentDir).truncatedTo(ChronoUnit.SECONDS)),
                () -> assertEquals(parentDir.getModifiedDateTime(), getModifiedDateTime(rootParentDir).truncatedTo(ChronoUnit.SECONDS)),
                () -> assertEquals(parentDir.getParent(), rootKeepFile),
                () -> assertEquals(parentDir.getSha256(), null),
                () -> assertFalse(parentDir.isRoot()),
                () -> assertFalse(parentDir.isDeleted())
        );
    }

    public LocalDateTime getModifiedDateTime(Path path) {
        try {
            return LocalDateTime.ofInstant((Files.getLastModifiedTime(path)).toInstant(), ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public LocalDateTime getCreatedDateTime(Path path) {
        try {
            return LocalDateTime.ofInstant(((FileTime) Files.getAttribute(path, "creationTime")).toInstant(), ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public String getSha256(Path path) {
        try{
            ByteSource byteSource = com.google.common.io.Files.asByteSource(path.toFile());
            HashCode hc = byteSource.hash(Hashing.sha256());
            return hc.toString();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }
}
