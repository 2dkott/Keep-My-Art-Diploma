package com.kivanov.diploma.contollers;

import com.kivanov.diploma.model.KeepProject;
import com.kivanov.diploma.model.KeepSource;
import com.kivanov.diploma.model.WebUrls;
import com.kivanov.diploma.services.YandexService;
import com.kivanov.diploma.services.YandexUserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Controller
@RequestMapping("/" + WebUrls.OAUTH2)
@SessionAttributes({"newProjectSession"})
public class OauthController {

    @Autowired
    YandexService yandexService;

    @GetMapping("/" + WebUrls.YANDEX)
    public RedirectView getAuthCodeFromYandex(@RequestParam("code") String code,
                                              @ModelAttribute("newProjectSession") NewProjectSession newProjectSession) throws IOException, ExecutionException, InterruptedException {

        yandexService.doOauth(code);
        YandexUserInfo yandexUserInfo = yandexService.getUserInfo(yandexService.getOauthToken());
        KeepSource keepSource = new KeepSource();
        keepSource.setCloud(true);
        keepSource.setUserToken(yandexService.getOauthToken());
        keepSource.setUserName(yandexUserInfo.getLogin());
        keepSource.setPath("app:");
        newProjectSession.getKeepSourceList().add(keepSource);

        return new RedirectView("/" + WebUrls.PROJECT + "/" + WebUrls.NEW);
    }

    @PostMapping("/" + WebUrls.REDIRECT + "/{cloud-name}")
    public RedirectView redirect(@PathVariable("cloud-name") String cloudName, @ModelAttribute("newProjectSession") NewProjectSession newProjectSession) {
        if (cloudName.equals(WebUrls.YANDEX)) return new RedirectView(yandexService.getAuthorizationUrl());
        return null;
    }

    @ModelAttribute("newProjectSession")
    public NewProjectSession newProjectSession() {
        return new NewProjectSession();
    }
}
