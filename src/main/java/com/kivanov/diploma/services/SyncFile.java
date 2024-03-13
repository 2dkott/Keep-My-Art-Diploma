package com.kivanov.diploma.services;

import com.kivanov.diploma.model.KeepFile;

import java.time.LocalDateTime;

public interface SyncFile {
    String getName();

    LocalDateTime getModifiedDateTime();

    LocalDateTime getCreatedDateTime();

    String getSha256();

    boolean isDirectory();

    KeepFile mapToKeepFile(KeepFile parent);

}
