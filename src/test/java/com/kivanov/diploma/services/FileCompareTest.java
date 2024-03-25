package com.kivanov.diploma.services;

import com.kivanov.diploma.model.KeepFile;
import com.kivanov.diploma.model.KeepFileSourceComparator;
import com.kivanov.diploma.model.KeepSource;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class FileCompareTest {

    final static String DIR_A = "DirA";
    final static String FILE_A = "FileA";
    final static String FILE_AA = "FileAA";
    final static String DIR_B = "DirB";
    final static String FILE_B = "FileB";
    final static String FILE_BB = "FileBB";
    final static String DIR_C = "DirC";
    final static String FILE_C = "FileC";
    final KeepSource source = new KeepSource();
    static LocalDateTime localDateTime = LocalDateTime.now();

    @Test
    public void compareTest() {
        Pair<List<KeepFile>, List<KeepFile>> lists = prepareLists();

        KeepFile leftListRoot = lists.getLeft().stream().filter(keepFile -> keepFile.getName().equals(DIR_A)).findFirst().get();

        KeepFile leftDirC = new KeepFile();
        leftDirC.setName(DIR_C);
        leftDirC.setDirectory(true);
        leftDirC.setParent(leftListRoot);

        KeepFile leftFileC = new KeepFile();
        leftFileC.setName(FILE_C);
        leftFileC.setSha256("C");
        leftFileC.setDirectory(false);
        leftFileC.setCreationDateTime(localDateTime);
        leftFileC.setModifiedDateTime(localDateTime);
        leftFileC.setParent(leftDirC);

        lists.getLeft().add(leftDirC);
        lists.getLeft().add(leftFileC);

        List<KeepFile> expectedLeftNotMatchedList = new ArrayList<>();
        expectedLeftNotMatchedList.add(leftDirC);
        expectedLeftNotMatchedList.add(leftFileC);

        KeepFile rightFileA = lists.getRight().stream().filter(keepFile -> keepFile.getName().equals(FILE_A)).findFirst().get();
        rightFileA.setSha256("AAAAAAAAA");

        KeepFile leftFileA = lists.getLeft().stream().filter(keepFile -> keepFile.getName().equals(FILE_A)).findFirst().get();
        KeepFile leftDirB = lists.getLeft().stream().filter(keepFile -> keepFile.getName().equals(DIR_B)).findFirst().get();
        KeepFile leftFileB = lists.getLeft().stream().filter(keepFile -> keepFile.getName().equals(FILE_B)).findFirst().get();
        KeepFile leftFileBB = lists.getLeft().stream().filter(keepFile -> keepFile.getName().equals(FILE_BB)).findFirst().get();
        lists.getLeft().remove(leftDirB);
        lists.getLeft().remove(leftFileB);
        lists.getLeft().remove(leftFileBB);

        List<KeepFile> expectedRightNotMatchedList = new ArrayList<>();
        expectedRightNotMatchedList.add(leftDirB);
        expectedRightNotMatchedList.add(leftFileB);
        expectedRightNotMatchedList.add(leftFileBB);

        List<Pair<KeepFile,KeepFile>> expectedModifiedList = new ArrayList<>();
        expectedModifiedList.add(Pair.of(leftFileA, rightFileA));

        KeepFileSourceComparator comparator = new KeepFileSourceComparator();
        comparator.compareLeftToRightSource(
                (left) -> lists.getLeft().stream().filter(keepFile -> {
                    if(Objects.nonNull(keepFile.getParent())) return keepFile.getParent().getName().equals(left.getName());
                    else return false;
                }).toList(),
                (right) -> lists.getRight().stream().filter(keepFile -> {
                    if(Objects.nonNull(keepFile.getParent())) return keepFile.getParent().getName().equals(right.getName());
                    else return false;
                }).toList(),
                (leftFile, rightFile) -> leftFile.getSha256().equals(rightFile.getSha256()),
                lists.getLeft().get(0),
                lists.getRight().get(0),
                source
        );

        assertThat(expectedLeftNotMatchedList).isEqualTo(comparator.getLeftNotMatchedFileList());
        assertThat(expectedRightNotMatchedList).isEqualTo(comparator.getRightNotMatchedFileList());
        assertThat(expectedModifiedList).isEqualTo(comparator.getModifiedFileList());
    }


    private static Pair<List<KeepFile>, List<KeepFile>> prepareLists() {
        KeepFile rightFileDirA = new KeepFile();
        rightFileDirA.setDirectory(true);
        rightFileDirA.setCreationDateTime(localDateTime);
        rightFileDirA.setModifiedDateTime(localDateTime);
        rightFileDirA.setName(DIR_A);

        KeepFile rightFileA = new KeepFile();
        rightFileA.setName(FILE_A);
        rightFileA.setSha256("A");
        rightFileA.setDirectory(false);
        rightFileA.setCreationDateTime(localDateTime);
        rightFileA.setModifiedDateTime(localDateTime);
        rightFileA.setParent(rightFileDirA);

        KeepFile rightFileAA = new KeepFile();
        rightFileAA.setName(FILE_AA);
        rightFileAA.setSha256("AA");
        rightFileAA.setDirectory(false);
        rightFileAA.setCreationDateTime(localDateTime);
        rightFileAA.setModifiedDateTime(localDateTime);
        rightFileAA.setParent(rightFileDirA);

        KeepFile rightFileDirB = new KeepFile();
        rightFileDirB.setName(DIR_B);
        rightFileDirB.setDirectory(true);
        rightFileDirB.setCreationDateTime(localDateTime);
        rightFileDirB.setModifiedDateTime(localDateTime);
        rightFileDirB.setParent(rightFileDirA);

        KeepFile rightFileB = new KeepFile();
        rightFileB.setName(FILE_B);
        rightFileB.setSha256("B");
        rightFileB.setDirectory(false);
        rightFileB.setCreationDateTime(localDateTime);
        rightFileB.setModifiedDateTime(localDateTime);
        rightFileB.setParent(rightFileDirB);

        KeepFile rightFileBB = new KeepFile();
        rightFileBB.setName(FILE_BB);
        rightFileBB.setSha256("BB");
        rightFileBB.setDirectory(false);
        rightFileBB.setCreationDateTime(localDateTime);
        rightFileBB.setModifiedDateTime(localDateTime);
        rightFileBB.setParent(rightFileDirB);

        List<KeepFile> rightList = new ArrayList<>();
        rightList.add(rightFileDirA);
        rightList.add(rightFileA);
        rightList.add(rightFileAA);
        rightList.add(rightFileDirB);
        rightList.add(rightFileB);
        rightList.add(rightFileBB);

        KeepFile leftFileDirA = new KeepFile();
        leftFileDirA.setDirectory(true);
        leftFileDirA.setName(DIR_A);
        leftFileDirA.setCreationDateTime(localDateTime);
        leftFileDirA.setModifiedDateTime(localDateTime);

        KeepFile leftFileA = new KeepFile();
        leftFileA.setName(FILE_A);
        leftFileA.setSha256("A");
        leftFileA.setDirectory(false);
        leftFileA.setCreationDateTime(localDateTime);
        leftFileA.setModifiedDateTime(localDateTime);
        leftFileA.setParent(rightFileDirA);

        KeepFile leftFileAA = new KeepFile();
        leftFileAA.setName(FILE_AA);
        leftFileAA.setSha256("AA");
        leftFileAA.setDirectory(false);
        leftFileAA.setCreationDateTime(localDateTime);
        leftFileAA.setModifiedDateTime(localDateTime);
        leftFileAA.setParent(rightFileDirA);

        KeepFile leftFileDirB = new KeepFile();
        leftFileDirB.setName(DIR_B);
        leftFileDirB.setDirectory(true);
        leftFileDirB.setCreationDateTime(localDateTime);
        leftFileDirB.setModifiedDateTime(localDateTime);
        leftFileDirB.setParent(rightFileDirA);

        KeepFile leftFileB = new KeepFile();
        leftFileB.setName(FILE_B);
        leftFileB.setSha256("B");
        leftFileB.setDirectory(false);
        leftFileB.setCreationDateTime(localDateTime);
        leftFileB.setModifiedDateTime(localDateTime);
        leftFileB.setParent(rightFileDirB);

        KeepFile leftFileBB = new KeepFile();
        leftFileBB.setName(FILE_BB);
        leftFileBB.setSha256("BB");
        leftFileBB.setDirectory(false);
        leftFileBB.setCreationDateTime(localDateTime);
        leftFileBB.setModifiedDateTime(localDateTime);
        leftFileBB.setParent(rightFileDirB);

        List<KeepFile> leftList = new ArrayList<>();
        leftList.add(leftFileDirA);
        leftList.add(leftFileA);
        leftList.add(leftFileAA);
        leftList.add(leftFileDirB);
        leftList.add(leftFileB);
        leftList.add(leftFileBB);

        return Pair.of(leftList, rightList);
    }
}
