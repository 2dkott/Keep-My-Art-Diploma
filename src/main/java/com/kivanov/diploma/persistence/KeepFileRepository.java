package com.kivanov.diploma.persistence;

import com.kivanov.diploma.model.KeepFile;
import com.kivanov.diploma.model.KeepSource;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface KeepFileRepository extends CrudRepository<KeepFile, Long> {
    List<KeepFile> findKeepFileBySource(KeepSource source);

    List<KeepFile> findKeepFileByParent(KeepFile parent);

    List<KeepFile> findKeepFileByNameAndSource(String name, KeepSource source);

}
