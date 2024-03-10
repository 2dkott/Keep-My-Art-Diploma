package com.kivanov.diploma.services;

import java.time.LocalDateTime;

public interface SyncFile {
    String getName();
    LocalDateTime getModifiedDateTime();
    LocalDateTime getCreatedDateTime();
    String getSha256();
    boolean isDirectory();


}
