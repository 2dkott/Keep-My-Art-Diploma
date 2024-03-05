package com.kivanov.diploma.persistence;

import com.kivanov.diploma.model.KeepFile;
import com.kivanov.diploma.model.KeepSource;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;

public interface KeepFileRepository extends CrudRepository<KeepFile, Long> {
    Collection<? extends KeepFile> findKeepFileBySource(KeepSource source);
}
