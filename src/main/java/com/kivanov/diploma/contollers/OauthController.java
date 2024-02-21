package com.kivanov.diploma.contollers;

import com.kivanov.diploma.model.WebUrls;
import com.kivanov.diploma.services.YandexService;
import com.kivanov.diploma.services.YandexUserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@Controller
@RequestMapping("/" + WebUrls.OAUTH2)
public class OauthController {

    @Autowired
    YandexService yandexService;

    @GetMapping("/" + WebUrls.YANDEX)
    public String getAuthCodeFromYandex(@RequestParam("code") String code, Model model) throws IOException, ExecutionException, InterruptedException {
        yandexService.doOauth(code);
        model.addAttribute("oauth", code);
        return "new-project";
    }
}
