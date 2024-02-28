package com.kivanov.diploma.services;

import com.kivanov.diploma.model.KeepSource;
import com.kivanov.diploma.persistence.KeepSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class SourceService {

    @Autowired
    KeepSourceRepository repository;

    public void createAll(List<KeepSource> sources) {
        repository.saveAll(sources);
    }

    public KeepSource removeKeepSourceById(long id) throws NoKeepSourceException {
        Optional<KeepSource> sourceToDelete = repository.findById(id);
        sourceToDelete.ifPresent(keepSource -> repository.delete(keepSource));
        return sourceToDelete.orElseThrow(() -> new NoKeepSourceException(id));
    }

}
