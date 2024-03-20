package com.kivanov.diploma.contollers;

import com.kivanov.diploma.model.KeepFile;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Data
public class KeepFileModifiedModel {
    private KeepFile leftFile;
    private KeepFile rightFile;
    private boolean checked;

    public KeepFileModifiedModel(KeepFile leftFile, KeepFile rightFile) {
        this.leftFile = leftFile;
        this.rightFile = rightFile;
    }

    public boolean leftIsCloud() {
        return leftFile.getSource().isCloud();
    }

    public boolean rightIsCloud() {
        return rightFile.getSource().isCloud();
    }

    public String getLeftCreationDateTime() {
        return buildDataTime(leftFile.getCreationDateTime());
    }

    public String getLeftModificationDateTime() {
        return buildDataTime(leftFile.getModifiedDateTime());
    }

    public String getRightCreationDateTime() {
        return buildDataTime(rightFile.getCreationDateTime());
    }

    public String getRightModificationDateTime() {
        return buildDataTime(rightFile.getModifiedDateTime());
    }

    private String buildDataTime(LocalDateTime fileLocalDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return Objects.nonNull(fileLocalDateTime) ? fileLocalDateTime.format(formatter) : "";
    }

    public String getFileName() {
        return leftFile.getName();
    }

    public String getFilePath() {
        return leftFile.getPathId();
    }

}
