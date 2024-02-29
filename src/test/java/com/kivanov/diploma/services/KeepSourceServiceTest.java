package com.kivanov.diploma.services;

import com.kivanov.diploma.model.KeepSource;
import com.kivanov.diploma.persistence.KeepSourceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
public class KeepSourceServiceTest {

    @Autowired
    SourceService keepSourceService;

    @MockBean
    KeepSourceRepository keepSourceRepository;

    private KeepSource createLocalPathSource() {
        KeepSource localSource = new KeepSource();
        localSource.setCloud(false);
        localSource.setPath("D:/Test");
        return localSource;
    }

    private KeepSource createCloudSource() {
        KeepSource cloudKeepSource = new KeepSource();
        cloudKeepSource.setUserName("TestUserName");
        cloudKeepSource.setCloud(true);
        cloudKeepSource.setUserToken("TestTokenD");
        cloudKeepSource.setPath("D:/Test");
        return cloudKeepSource;
    }


    public Stream<Arguments> keepSourceProvider() {
        return Stream.of(
                Arguments.arguments("Save Cloud Resource", createCloudSource()),
                Arguments.arguments("Save Local Path Resource", createLocalPathSource())
                );
    }

    public Stream<Arguments> keepSourceListProvider() {
        return Stream.of(
                Arguments.arguments("Save list with single source", List.of(createCloudSource())),
                Arguments.arguments("Save list with sources", List.of(createCloudSource(),createLocalPathSource()))
        );
    }

    @ParameterizedTest(name =" {0}")
    @MethodSource("keepSourceProvider")
    public void testSaveKeepSource(String testName, KeepSource expectedKeepSource) {
        when(keepSourceRepository.save(expectedKeepSource)).thenReturn(expectedKeepSource);
        KeepSource actualKeepSource = keepSourceService.saveKeepSource(expectedKeepSource);
        assert actualKeepSource.equals(expectedKeepSource);
    }

    @ParameterizedTest(name =" {0}")
    @MethodSource("keepSourceListProvider")
    public void testSaveAllKeepSource(String testName, List<KeepSource> expectedKeepSourceList) {
        when(keepSourceRepository.saveAll(expectedKeepSourceList)).thenReturn(expectedKeepSourceList);
        List<KeepSource> actualKeepSources = (List<KeepSource>) keepSourceService.saveKeepSources(expectedKeepSourceList);
        assert actualKeepSources.equals(expectedKeepSourceList);
    }

    @Test
    public void testSuccessFindSourceById() throws NoKeepSourceException {
        KeepSource expectedSource = createCloudSource();
        when(keepSourceRepository.findById(1l)).thenReturn(Optional.of(expectedSource));
        KeepSource actualKeepSource = keepSourceService.findKeepSourceById(1l);
        assert actualKeepSource.equals(expectedSource);
    }

    @Test
    public void testFailFindSourceById() {
        Exception actualException = assertThrows(NoKeepSourceException.class, () -> keepSourceService.findKeepSourceById(1000L));
        assert actualException.getMessage().equals(new NoKeepSourceException(1000L).getMessage());
    }

    @Test
    public void testDeletSourceById() throws NoKeepSourceException {
        KeepSource expectedSource = createCloudSource();
        when(keepSourceRepository.findById(1l)).thenReturn(Optional.of(expectedSource));
        KeepSource actualKeepSource = keepSourceService.removeKeepSourceById(1l);
        verify(keepSourceRepository, times(1)).delete(expectedSource);
        assert actualKeepSource.equals(expectedSource);
    }
}
