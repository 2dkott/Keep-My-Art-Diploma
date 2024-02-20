package com.kivanov.diploma.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class KeepSource {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    @Column
    private boolean isCloud;

    @Column
    private String path;

    @Column
    private String userName;

    @Column
    private String userToken;
}
