package com.kivanov.diploma.contoller;


import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
public class KeepProjectModel {
    public List<KeepFileModel> newFileList = new ArrayList<>();
    public List<KeepFileModifiedModel> modifiedNewFileList = new ArrayList<>();

    public void resetNewFileListWith(List<KeepFileModel> fileList) {
        newFileList.clear();
        newFileList.addAll(fileList);
    }

    public void resetModifiedNewFileList(List<KeepFileModifiedModel> fileList) {
        modifiedNewFileList.clear();
        modifiedNewFileList.addAll(fileList);
    }

}
