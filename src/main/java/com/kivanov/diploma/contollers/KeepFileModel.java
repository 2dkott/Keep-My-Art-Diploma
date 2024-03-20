package com.kivanov.diploma.contollers;

import com.kivanov.diploma.model.KeepFile;
import lombok.Data;

import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Data
public class KeepFileModel {
    private KeepFile file;
    private boolean checked;

    public boolean isCloud() {
        return file.getSource().isCloud();
    }


    public String getFileName() {
        return file.getName();
    }

    public String getFilePath() {
        return file.getPathId();
    }

}
