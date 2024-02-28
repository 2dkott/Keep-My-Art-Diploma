package com.kivanov.diploma.services;

import com.kivanov.diploma.model.KeepProject;
import com.kivanov.diploma.persistence.KeepProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ProjectService {

    @Autowired
    KeepProjectRepository repository;

    public void createProject(KeepProject project) {
        repository.save(project);
    }

    public Optional<KeepProject> findProject(long id) {
        return repository.findById(id);
    }
}
