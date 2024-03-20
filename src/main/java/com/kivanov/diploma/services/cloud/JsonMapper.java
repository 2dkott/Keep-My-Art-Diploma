package com.kivanov.diploma.services.cloud;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.kivanov.diploma.services.cloud.yandex.YandexFile;

import java.util.ArrayList;
import java.util.List;

public class JsonMapper {
    private static Gson gson = new Gson();

    public static List<YandexFile> mapJsonToYandexFiles(String jsonString) {
        JsonElement jsonWithFiles = JsonParser.parseString(jsonString).getAsJsonObject().get("_embedded").getAsJsonObject().get("items").getAsJsonArray();
        return gson.fromJson(jsonWithFiles, new TypeToken<ArrayList<YandexFile>>(){}.getType());
    }

    public static String mapJsonToUploadUrl(String jsonString) {
        JsonElement jsonHref = JsonParser.parseString(jsonString).getAsJsonObject().get("href");
        return gson.fromJson(jsonHref, String.class);
    }
}
