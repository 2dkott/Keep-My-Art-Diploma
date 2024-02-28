package com.kivanov.diploma.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "keepProjects")
public class KeepProject {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    @Column
    private String name;

    @Column
    private LocalDateTime creationTime;

    @OneToMany(mappedBy = "project",cascade=CascadeType.ALL, fetch=FetchType.LAZY)
    private List<KeepSource> keepSources;

}
