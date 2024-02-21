package com.kivanov.diploma.contollers;

import com.kivanov.diploma.model.WebUrls;
import com.kivanov.diploma.services.YandexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/" + WebUrls.PROJECT)
public class ProjectController {

    @Autowired
    YandexService yandexService;

    @GetMapping("/" + WebUrls.NEW)
    public String addNewProject(Model model) {
        model.addAttribute("yandexOauthUrl", yandexService.getAuthorizationUrl());
        return "new-project";
    }
}
