package com.kivanov.diploma.contoller;

import com.kivanov.diploma.model.KeepFile;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class KeepFileModelMapper {

    public static List<KeepFileModel> mapToKeepFileModelList(List<KeepFile> fileList) {
        return fileList.stream().filter(keepFile -> !keepFile.isDirectory()).map(KeepFileModelMapper::mapKeepFileToKeepFileModel).toList();
    }

    public static List<KeepFileModifiedModel> mapToKeepFileModifiedModelList(List<Pair<KeepFile,KeepFile>> fileList) {
        return fileList.stream()
                .filter(keepFileKeepFilePair -> !keepFileKeepFilePair.getLeft().isDirectory())
                .map(keepFileKeepFilePair -> new KeepFileModifiedModel(keepFileKeepFilePair.getLeft(), keepFileKeepFilePair.getRight()))
                .toList();
    }

    public static  KeepFileModel mapKeepFileToKeepFileModel(KeepFile file) {
        KeepFileModel keepFileModel = new KeepFileModel();
        keepFileModel.setFile(file);
        return keepFileModel;
    }
}
