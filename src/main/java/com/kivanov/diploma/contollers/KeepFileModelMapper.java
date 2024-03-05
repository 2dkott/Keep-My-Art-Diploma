package com.kivanov.diploma.contollers;

import com.kivanov.diploma.model.KeepFile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class KeepFileModelMapper {

    public List<ModelKeepFile> getFileList(List<KeepFile> fileList) {
        return fileList.stream().filter(keepFile -> !keepFile.isDirectory()).map(this::mapToModel).toList();
    }

    public ModelKeepFile mapToModel(KeepFile file) {
        StringBuffer stringBuffer = new StringBuffer();
        List<KeepFile> parents = new ArrayList<>();
        buildPath(parents, file);
        parents.reversed().stream().filter(keepFile -> !Objects.isNull(keepFile.getName())).forEach(keepFile -> {
            stringBuffer.append("/").append(keepFile.getName());
        });
        ModelKeepFile modelKeepFile = new ModelKeepFile();
        modelKeepFile.setFile(file);
        modelKeepFile.setFullPath(stringBuffer.toString());
        modelKeepFile.setParentDirectory(file.getParent());
        return modelKeepFile;
    }

    private void buildPath(List<KeepFile> parents, KeepFile keepFile) {
        KeepFile parent = keepFile.getParent();
        if (!Objects.isNull(parent)) {
            parents.add(parent);
            buildPath(parents, parent);
        }
    }

}
