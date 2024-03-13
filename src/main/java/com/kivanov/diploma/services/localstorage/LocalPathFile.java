package com.kivanov.diploma.services.localstorage;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;
import com.kivanov.diploma.model.KeepFile;
import com.kivanov.diploma.services.SyncFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Slf4j
@Data
@AllArgsConstructor
public class LocalPathFile implements SyncFile {

    private Path path;

    @Override
    public String getName() {
        return path.getFileName().toString();
    }

    @Override
    public LocalDateTime getModifiedDateTime() {
        try {
            return LocalDateTime.ofInstant((Files.getLastModifiedTime(path)).toInstant(), ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    @Override
    public LocalDateTime getCreatedDateTime() {
        try {
            return LocalDateTime.ofInstant(((FileTime) Files.getAttribute(path, "creationTime")).toInstant(), ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    @Override
    public boolean isDirectory() {
        return Files.isDirectory(path);
    }
    @Override
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
    public String getSha256() {
        try{
            ByteSource byteSource = com.google.common.io.Files.asByteSource(path.toFile());
            HashCode hc = byteSource.hash(Hashing.sha256());
            return hc.toString();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }
}
