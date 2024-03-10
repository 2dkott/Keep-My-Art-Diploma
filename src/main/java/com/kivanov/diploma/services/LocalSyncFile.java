package com.kivanov.diploma.services;

import lombok.Data;

import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
public class LocalSyncFile implements SyncFile {

    private String name;
    private String sha256;
    private boolean isDirectory;
    private FileTime created;
    private FileTime modified;

    @Override
    public LocalDateTime getModifiedDateTime() {
        return LocalDateTime.ofInstant(modified.toInstant(), ZoneId.systemDefault());
    }

    @Override
    public LocalDateTime getCreatedDateTime() {
        return LocalDateTime.ofInstant(created.toInstant(), ZoneId.systemDefault());
    }
}
