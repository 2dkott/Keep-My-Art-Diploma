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

    public Iterable<KeepSource> saveKeepSources(List<KeepSource> sources) {
        return repository.saveAll(sources);
    }

    public KeepSource saveKeepSource(KeepSource keepSource) {
        return repository.save(keepSource);
    }

    public KeepSource findKeepSourceById(long id) throws NoKeepSourceException {
        return repository.findById(id).orElseThrow(() -> new NoKeepSourceException(id));
    }

    public KeepSource removeKeepSourceById(long id) throws NoKeepSourceException {
        Optional<KeepSource> sourceToDelete = repository.findById(id);
        return sourceToDelete.map(source -> {
            repository.delete(source);
            return source;
        }).orElseThrow(() -> new NoKeepSourceException(id));
    }

}
