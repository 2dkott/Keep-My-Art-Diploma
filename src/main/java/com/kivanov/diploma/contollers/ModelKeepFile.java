package com.kivanov.diploma.contollers;

import com.kivanov.diploma.model.KeepFile;
import lombok.Data;

@Data
public class ModelKeepFile {
    private KeepFile file;
    private KeepFile parentDirectory;
    private String fullPath;

}
