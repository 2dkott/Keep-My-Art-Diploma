package com.kivanov.diploma.services;

import java.time.LocalDateTime;

public interface CloudFile {
    String getFileName();
    LocalDateTime getUpdated();
    LocalDateTime getCreated();
    boolean isDirectory();
}
