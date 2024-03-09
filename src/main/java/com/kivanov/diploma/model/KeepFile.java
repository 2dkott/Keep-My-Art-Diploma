package com.kivanov.diploma.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "keepFiles")
public class KeepFile {

    public static final String ROOT = "ROOT";

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

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

    @ManyToOne
    @JoinColumn(name = "source_id")
    private KeepSource source;

    public KeepFile() {
    }

    public static KeepFile Root() {
        KeepFile root = new KeepFile();
        root.setName(ROOT);
        return root;
    }
}
