package com.kivanov.diploma.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class KeepFile {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    @Column
    private String name;

    @Column
    @OneToOne
    private KeepFile parent;

    @Column
    private LocalDateTime creationTime;

    @Column
    private LocalDateTime updateTime;

    @Column
    private boolean isDeleted;

    public KeepFile() {
    }
}
