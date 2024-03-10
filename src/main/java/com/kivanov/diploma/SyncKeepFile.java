package com.kivanov.diploma;

import com.kivanov.diploma.model.KeepFile;
import lombok.Data;

@Data
public class SyncKeepFile {

    KeepFile keepFile;
    private String fileName;
    private String filePath;
    private boolean isNew;
    private boolean isDeleted;
    private boolean isModified;

}
