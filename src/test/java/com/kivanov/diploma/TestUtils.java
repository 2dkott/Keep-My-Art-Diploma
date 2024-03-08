package com.kivanov.diploma;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.kivanov.diploma.model.KeepProject;
import com.kivanov.diploma.model.KeepSource;
import com.kivanov.diploma.model.SourceType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Random;

@Component
public class TestUtils {

    public static KeepSource createLocalPathSource(KeepProject project) {
        KeepSource localSource = new KeepSource();
        localSource.setType(SourceType.LOCAL);
        localSource.setPath("D:/Test");
        localSource.setProject(project);
        return localSource;
    }

    public static KeepSource createCloudSource(KeepProject project) {
        KeepSource cloudKeepSource = new KeepSource();
        cloudKeepSource.setUserName(String.format("TestUserName%s", new Random().nextInt(1000)));
        cloudKeepSource.setType(SourceType.YANDEX);
        cloudKeepSource.setUserToken(String.format("TestToken%s", new Random().nextInt(1000)));
        cloudKeepSource.setPath("D:/Test");
        cloudKeepSource.setProject(project);
        return cloudKeepSource;
    }

    public static KeepProject createProject() {
        KeepProject keepProject = new KeepProject();
        keepProject.setName(String.format("TestProject%s", new Random().nextInt(1000)));
        return keepProject;
    }

    public static String readFile(String filePath) throws IOException {
        return Resources.toString(Resources.getResource(filePath),  Charsets.UTF_8);
    }
}
