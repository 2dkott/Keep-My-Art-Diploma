package com.kivanov.diploma.services;

import com.kivanov.diploma.TestUtils;
import com.kivanov.diploma.model.KeepProject;
import com.kivanov.diploma.persistence.KeepProjectRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
public class KeepProjectServiceTest {

    @Autowired
    TestUtils testUtils;

    @MockBean
    KeepProjectRepository keepProjectRepository;

    @Autowired
    ProjectService projectService;

    @Test
    public void testSaveProject() {
        KeepProject expectedProject = TestUtils.createProject();
        when(keepProjectRepository.save(expectedProject)).thenReturn(expectedProject);
        KeepProject actualProject = projectService.saveProject(expectedProject);
        assert actualProject.equals(expectedProject);
    }

    @Test
    public void testGetAllProjects() {
        List<KeepProject> expectedProjectList = List.of(TestUtils.createProject(), TestUtils.createProject());
        when(keepProjectRepository.findAll()).thenReturn(expectedProjectList);
        List<KeepProject> actualProjectList = projectService.getAllKeepProjects();
        assert actualProjectList.equals(expectedProjectList);
    }

    @Test
    public void testSuccessFindProjectById() throws NoKeepProjectException {
        KeepProject expectedProject = TestUtils.createProject();
        when(keepProjectRepository.findById(1L)).thenReturn(Optional.of(expectedProject));
        KeepProject actualProject = projectService.findProjectById(1L);
        assert actualProject.equals(expectedProject);
    }

    @Test
    public void testFailFindProjectById() {
        Exception actualException = assertThrows(NoKeepProjectException.class, () -> projectService.findProjectById(1L));
        assert actualException.getMessage().equals(new NoKeepProjectException(1L).getMessage());
    }

    @Test
    public void testSuccessDeleteProjectById() throws NoKeepProjectException {
        KeepProject expectedProject = TestUtils.createProject();
        when(keepProjectRepository.findById(1l)).thenReturn(Optional.of(expectedProject));
        KeepProject actualProject = projectService.deleteProjectById(1l);
        verify(keepProjectRepository, times(1)).delete(expectedProject);
        assert actualProject.equals(expectedProject);
    }

    @Test
    public void testFailDeleteProjectById() {
        KeepProject expectedProject = TestUtils.createProject();
        Exception actualException = assertThrows(NoKeepProjectException.class, () -> projectService.deleteProjectById(1L));
        verify(keepProjectRepository, times(0)).delete(expectedProject);
        assert actualException.getMessage().equals(new NoKeepProjectException(1L).getMessage());
    }


}
