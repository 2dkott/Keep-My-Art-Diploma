package com.kivanov.diploma.persistence;

import com.kivanov.diploma.model.KeepFile;
import org.springframework.data.repository.CrudRepository;

public interface KeepFileRepository extends CrudRepository<KeepFile, Long> {
}
