package com.kivanov.diploma.services.cloud;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Map;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("urls")
@Data
public class UrlConfiguration {

    private Map<String, ResourceUrls> sources;

    public ResourceUrls getYandex() {
        return sources.get("yandex");
    }
}
