package com.kivanov.diploma.services.yandex;

import com.kivanov.diploma.services.CloudFile;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class YandexFile implements CloudFile {
    private String name;
    private String sha256;
    private String type;
    private String created;
    private String modified;

    @Override
    public String getFileName() {
        return name;
    }

    @Override
    public LocalDateTime getUpdated() {
        return LocalDateTime.parse(modified, DateTimeFormatter.ISO_DATE_TIME);
    }

    @Override
    public LocalDateTime getCreated() {
        return LocalDateTime.parse(created, DateTimeFormatter.ISO_DATE_TIME);
    }

    @Override
    public boolean isDirectory() {
        return type.equals("dir");
    }
}
