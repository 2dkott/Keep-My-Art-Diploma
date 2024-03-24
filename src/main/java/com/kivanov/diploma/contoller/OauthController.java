package com.kivanov.diploma.contoller;

import com.kivanov.diploma.model.KeepSource;
import com.kivanov.diploma.model.SourceType;
import com.kivanov.diploma.model.WebUrls;
import com.kivanov.diploma.services.cloud.yandex.YandexService;
import com.kivanov.diploma.services.cloud.yandex.YandexUserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@Controller
@RequestMapping("/" + WebUrls.OAUTH2)
@SessionAttributes({"newProjectSession"})
public class OauthController {

    @Autowired
    YandexService yandexService;

    @Value("${services.api.yandex.app_path}")
    private String appPath;

    @GetMapping("/" + WebUrls.YANDEX)
    public RedirectView getAuthCodeFromYandex(@RequestParam("code") String code,
                                              @ModelAttribute("newProjectSession") NewProjectSession newProjectSession) throws IOException, ExecutionException, InterruptedException {

        yandexService.doOauth(code);
        YandexUserInfo yandexUserInfo = yandexService.getUserInfo(yandexService.getOauthToken());
        KeepSource keepSource = new KeepSource();
        keepSource.setType(SourceType.YANDEX);
        keepSource.setUserToken(yandexService.getOauthToken());
        keepSource.setUserName(yandexUserInfo.getLogin());
        keepSource.setPath(appPath);
        keepSource.setClone(false);
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
