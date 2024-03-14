package com.kivanov.diploma.services.localstorage;

import com.kivanov.diploma.services.FileDealingException;

public class LocalFileReadingException extends FileDealingException {

    public LocalFileReadingException(String message) {
        super(String.format("Произашла ошибка чтения данных с локального хранилища \n%s", message));
    }
}
