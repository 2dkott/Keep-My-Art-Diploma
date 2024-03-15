package com.kivanov.diploma.model;

import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

@Data
public class SyncKeepFileData {
    List<KeepFile> newLocalFiles = new ArrayList<>();
    List<KeepFile> newCloudFiles = new ArrayList<>();
    List<Pair<KeepFile, KeepFile>> modifiedFiles = new ArrayList<>();
}
