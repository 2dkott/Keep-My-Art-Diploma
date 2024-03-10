package com.kivanov.diploma.model;

import jakarta.persistence.Column;

public class RootKeepFile extends KeepFile{
    public RootKeepFile() {
        super();
        this.setName("ROOT");
    }
}
