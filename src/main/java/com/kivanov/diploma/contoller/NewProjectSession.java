package com.kivanov.diploma.contoller;

import com.kivanov.diploma.model.KeepSource;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class NewProjectSession {

    @NotEmpty(message = "Название проекта не должно быть пустым")
    @Size(min = 3, max = 250)
    private String projectName;

    @NotEmpty(message = "Путь к Вашему проекту на локальном диске не может быть пустым")
    @Pattern(regexp = "^(?:[a-zA-Z]:|(\\|/)).*", message = "Не корректный формат пути к проекту")
    @Size(min = 2)
    private String localPath;

    @NotEmpty(message = "Вы должны добавить хотя бы одно хранилще для Вашего проекта")
    private final List<KeepSource> keepSourceList = new ArrayList<>();
}
