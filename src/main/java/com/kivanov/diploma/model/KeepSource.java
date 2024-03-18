package com.kivanov.diploma.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "keepSources")
public class KeepSource {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    @Column
    private boolean isClone;

    @Column
    private String path;

    @Column
    private String userName;

    @Column
    private String userToken;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private KeepProject project;

    @OneToMany(mappedBy = "source",cascade=CascadeType.ALL, fetch=FetchType.LAZY)
    private List<KeepFile> keepSources;

    @Column
    private SourceType type;

    public boolean isCloud() {
        return !type.equals(SourceType.LOCAL);
    }

    @Override
    public String toString() {
        return String.format("id:%s,type:%s", id, type);
    }
}
