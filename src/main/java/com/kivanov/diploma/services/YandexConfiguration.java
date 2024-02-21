package com.kivanov.diploma.services;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class YandexConfiguration {

    @Bean
    YandexService yandexService() {
        return new YandexService();
    }
}
