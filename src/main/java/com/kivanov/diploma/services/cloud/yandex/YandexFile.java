package com.kivanov.diploma.services.cloud.yandex;

import com.kivanov.diploma.model.KeepFile;
import com.kivanov.diploma.services.SyncFile;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Data
public class YandexFile implements SyncFile {
    private String name;
    private String sha256;
    private String type;
    private String created;
    private String modified;

    @Override
    public LocalDateTime getModifiedDateTime() {
        return LocalDateTime.parse(modified, DateTimeFormatter.ISO_DATE_TIME).truncatedTo(ChronoUnit.SECONDS);
    }

    @Override
    public LocalDateTime getCreatedDateTime() {
        return LocalDateTime.parse(created, DateTimeFormatter.ISO_DATE_TIME).truncatedTo(ChronoUnit.SECONDS);
    }

    @Override
    public boolean isDirectory() {
        return type.equals("dir");
    }

    public KeepFile mapToKeepFile(KeepFile parent) {
        KeepFile file = new KeepFile();
        file.setName(getName());
        file.setDirectory(isDirectory());
        file.setDeleted(false);
        file.setCreationDateTime(getCreatedDateTime());
        file.setModifiedDateTime(getModifiedDateTime());
        file.setSha256(getSha256());
        file.setSource(parent.getSource());
        file.setParent(parent);
        return file;
    }

    @Override
    public String toString() {
        return String.format("name:%s, type:%s", name, type);
    }
}
