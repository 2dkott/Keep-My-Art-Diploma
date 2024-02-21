package com.kivanov.diploma.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "keepFiles")
public class KeepFile {

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
    private LocalDateTime creationTime;

    @Column
    private LocalDateTime updateTime;

    @Column
    private boolean isDeleted;

    public KeepFile() {
    }
}
