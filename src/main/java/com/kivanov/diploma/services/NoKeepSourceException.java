package com.kivanov.diploma.services;

public class NoKeepSourceException extends Exception{
    public NoKeepSourceException(long id) {
        super(String.format("There is no source in database with such id: %s", id));
    }

    public NoKeepSourceException(String userName) {
        super(String.format("There is no source in database with user name: %s", userName));
    }
}
