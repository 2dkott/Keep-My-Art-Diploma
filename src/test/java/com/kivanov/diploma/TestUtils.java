package com.kivanov.diploma;

import com.kivanov.diploma.model.KeepProject;
import com.kivanov.diploma.model.KeepSource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
public class TestUtils {

    public KeepSource createLocalPathSource(KeepProject project) {
        KeepSource localSource = new KeepSource();
        localSource.setCloud(false);
        localSource.setPath("D:/Test");
        localSource.setProject(project);
        return localSource;
    }

    public KeepSource createCloudSource(KeepProject project) {
        KeepSource cloudKeepSource = new KeepSource();
        cloudKeepSource.setUserName(String.format("TestUserName%s", new Random().nextInt(1000)));
        cloudKeepSource.setCloud(true);
        cloudKeepSource.setUserToken(String.format("TestToken%s", new Random().nextInt(1000)));
        cloudKeepSource.setPath("D:/Test");
        cloudKeepSource.setProject(project);
        return cloudKeepSource;
    }

    public KeepProject createProject() {
        KeepProject keepProject = new KeepProject();
        keepProject.setName(String.format("TestProject%s", new Random().nextInt(1000)));
        return keepProject;
    }
}
