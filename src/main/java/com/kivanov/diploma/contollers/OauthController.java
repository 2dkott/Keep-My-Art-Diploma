package com.kivanov.diploma.contollers;

import com.kivanov.diploma.model.WebUrls;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/" + WebUrls.OAUTH2)
public class OauthController {

    @GetMapping("/" + WebUrls.YANDEX)
    public void getAuthCodeFromYandex() {

    }
}
