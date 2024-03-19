package com.kivanov.diploma.contollers;

import com.kivanov.diploma.model.KeepFile;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KeepFileModelMapper {

    public List<KeepFileModel> mapToKeepFileModelList(List<KeepFile> fileList) {
        return fileList.stream().filter(keepFile -> !keepFile.isDirectory()).map(this::mapKeepFileToKeepFileModel).toList();
    }

    public List<Pair<KeepFileModel, KeepFileModel>> mapToKeepFileModelPairList(List<Pair<KeepFile,KeepFile>> fileList) {
        return fileList.stream()
                .filter(keepFileKeepFilePair -> !keepFileKeepFilePair.getLeft().isDirectory())
                .map(keepFileKeepFilePair -> Pair.of(mapKeepFileToKeepFileModel(keepFileKeepFilePair.getLeft()), mapKeepFileToKeepFileModel(keepFileKeepFilePair.getRight()))).toList();
    }

    public KeepFileModel mapKeepFileToKeepFileModel(KeepFile file) {
        KeepFileModel keepFileModel = new KeepFileModel();
        keepFileModel.setFile(file);
        return keepFileModel;
    }
}
