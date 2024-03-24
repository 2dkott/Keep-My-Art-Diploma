package com.kivanov.diploma.contoller;

import com.kivanov.diploma.model.KeepFile;
import lombok.Data;

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
