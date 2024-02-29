package com.kivanov.diploma.services;

public class NoKeepProjectException extends Exception{
    public NoKeepProjectException(long id) {
        super(String.format("There is no project in database with such id: %s", id));
    }
}
