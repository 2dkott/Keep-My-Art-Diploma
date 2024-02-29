package com.kivanov.diploma.services;

import com.kivanov.diploma.model.KeepProject;
import com.kivanov.diploma.persistence.KeepProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ProjectService {

    @Autowired
    KeepProjectRepository repository;

    public KeepProject saveProject(KeepProject project) {
        return repository.save(project);
    }

    public List<KeepProject> getAllKeepProjects() {
        return (List<KeepProject>) repository.findAll();
    }

    public KeepProject findProjectById(long id) throws NoKeepProjectException{
        return repository.findById(id).orElseThrow(() -> new NoKeepProjectException(id));
    }

    public KeepProject deleteProjectById(long id) throws NoKeepProjectException{
        Optional<KeepProject> projectToDelete = repository.findById(id);
        return projectToDelete.map(project -> {
            repository.delete(project);
            return project;
        }).orElseThrow(() -> new NoKeepProjectException(id));
    }
}
