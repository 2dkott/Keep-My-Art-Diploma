package com.kivanov.diploma.model;

import jakarta.persistence.*;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Entity
@Data
@Table(name = "keepFiles")
public class KeepFile implements Cloneable {

    public static final String ROOT = "ROOT";

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    @Column
    private int version;

    @Column
    private String pathId;

    @Column
    private String name;

    @ManyToOne
    private KeepFile parent;

    @OneToMany(mappedBy="parent")
    private List<KeepFile> children;

    @Column
    private LocalDateTime creationDateTime;

    @Column
    private LocalDateTime modifiedDateTime;

    @Column
    private boolean isDeleted;

    @Column
    private boolean isDirectory;

    @Column
    private String sha256;

    @Column
    private boolean isRoot=false;

    @ManyToOne
    @JoinColumn(name = "source_id")
    private KeepSource source;

    public KeepFile() {
    }

    public String getPathId() {
        if(StringUtils.isEmpty(pathId)) return buildPathId();
        return pathId;
    }

    public void setCreationDateTime(LocalDateTime creationDateTime) {
        this.creationDateTime = creationDateTime.truncatedTo(ChronoUnit.SECONDS);
    }

    public void setModifiedDateTime(LocalDateTime modifiedDateTime) {
        this.modifiedDateTime = modifiedDateTime.truncatedTo(ChronoUnit.SECONDS);
    }

    public static KeepFile Root(KeepSource keepSource) {
        KeepFile root = new KeepFile();
        root.setRoot(true);
        root.setName(ROOT);
        root.setSource(keepSource);
        return root;
    }

    @PrePersist
    private void prePersistFunction(){
        this.pathId=buildPathId();
    }

    private String buildPathId(){
        return buildPath(new StringBuffer(), this).toString();
    }

    private StringBuffer buildPath(StringBuffer stringBuffer, KeepFile keepFile) {
        if(Objects.isNull(keepFile.getParent())) return stringBuffer;
        else buildPath(stringBuffer, keepFile.getParent());
        stringBuffer.append("/");
        stringBuffer.append(keepFile.getName());
        return stringBuffer;
    }

    @Override
    public String toString() {
        return getPathId();
    }

}
