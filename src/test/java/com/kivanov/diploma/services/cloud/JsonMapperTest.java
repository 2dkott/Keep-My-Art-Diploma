package com.kivanov.diploma.services.cloud;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.kivanov.diploma.services.cloud.yandex.YandexFile;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@SpringBootTest
public class JsonMapperTest {

    private final static String JSON_TEMPLATES_PATH = "json-templates/cloud/yandex/mapper/";
    private final static String JSON_1_FILE_1_DIR = "json-1-file-1-dir.json";
    private final static String JSON_1_FILE = "json-1-file.json";
    private final static String JSON_1_DIR = "json-1-dir.json";
    private final static String JSON_EMPTY = "json-empty.json";

    private static Stream<Arguments> yandexProvider() {
        return Stream.of(
                Arguments.arguments("Test with 1 file and 1 dir", JSON_TEMPLATES_PATH + JSON_1_FILE_1_DIR, get1File1DirYandexFiles()),
                Arguments.arguments("Test with 1 file", JSON_TEMPLATES_PATH + JSON_1_FILE, get1File1YandexFiles()),
                Arguments.arguments("Test with 1 dir", JSON_TEMPLATES_PATH + JSON_1_DIR, get1DirYandexFiles()),
                Arguments.arguments("Test with no any files or dirs", JSON_TEMPLATES_PATH + JSON_EMPTY, new ArrayList<>())
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("yandexProvider")
    public void testJsonToYandexFile(String testName, String template, List<YandexFile> expectedList) throws IOException {
        String jsonString = Resources.toString(Resources.getResource(template),  Charsets.UTF_8);
        List<YandexFile> actualList = JsonMapper.mapJsonToYandexFiles(jsonString);
        assert actualList.equals(expectedList);
    }

    private static List<YandexFile> get1File1DirYandexFiles() {
        YandexFile dir = new YandexFile();
        dir.setName("test1");
        dir.setType("dir");
        dir.setModified("2024-03-06T22:08:36+00:00");
        dir.setCreated("2024-03-06T22:08:36+00:00");

        YandexFile file = new YandexFile();
        file.setName("test_c1_1.txt");
        file.setSha256("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
        file.setType("file");
        file.setModified("2024-03-06T22:07:27+00:00");
        file.setCreated("2024-03-06T22:07:27+00:00");

        List<YandexFile> expectedList = new ArrayList<>();
        expectedList.add(dir);
        expectedList.add(file);
        return expectedList;
    }

    private static List<YandexFile> get1File1YandexFiles() {
        YandexFile file = new YandexFile();
        file.setName("test_c1_1.txt");
        file.setSha256("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
        file.setType("file");
        file.setModified("2024-03-06T22:07:27+00:00");
        file.setCreated("2024-03-06T22:07:27+00:00");

        List<YandexFile> expectedList = new ArrayList<>();
        expectedList.add(file);
        return expectedList;
    }

    private static List<YandexFile> get1DirYandexFiles() {
        YandexFile dir = new YandexFile();
        dir.setName("test1");
        dir.setType("dir");
        dir.setModified("2024-03-06T22:08:36+00:00");
        dir.setCreated("2024-03-06T22:08:36+00:00");

        List<YandexFile> expectedList = new ArrayList<>();
        expectedList.add(dir);
        return expectedList;
    }

}
