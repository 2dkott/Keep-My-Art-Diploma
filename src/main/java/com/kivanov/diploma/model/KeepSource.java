package com.kivanov.diploma.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "keepSources")
public class KeepSource {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    @Column
    private boolean isCloud;

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
}
