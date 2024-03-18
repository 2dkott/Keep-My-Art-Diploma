package com.kivanov.diploma.contollers;

import com.kivanov.diploma.model.KeepFile;
import lombok.Data;

import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Data
public class KeepFileModel {
    private KeepFile file;
    private String fileInfo;
    private String filePath;
    private boolean checked;

    public boolean isCloud() {
        return file.getSource().isCloud();
    }

    public String getCreationDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return Objects.nonNull(file.getCreationDateTime()) ? file.getCreationDateTime().format(formatter) : "";
    }

    public String getModificationDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return Objects.nonNull(file.getModifiedDateTime()) ? file.getCreationDateTime().format(formatter) : "";
    }

    public String getFileName() {
        return file.getName();
    }

    public String getFilePath() {
        return file.getPathId();
    }

}
