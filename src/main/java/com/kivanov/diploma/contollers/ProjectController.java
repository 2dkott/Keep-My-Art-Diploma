package com.kivanov.diploma.contollers;

import com.kivanov.diploma.model.KeepProject;
import com.kivanov.diploma.model.KeepSource;
import com.kivanov.diploma.model.WebUrls;
import com.kivanov.diploma.services.YandexService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/" + WebUrls.PROJECT)
@SessionAttributes({"newProjectSession"})
public class ProjectController {

    @Autowired
    YandexService yandexService;

    @GetMapping("/" + WebUrls.NEW)
    public String showNewProject(Model model, @ModelAttribute("newProjectSession") NewProjectSession newProjectSession) {
        model.addAttribute("yandexOauthUrl", yandexService.getAuthorizationUrl());
        return "new-project";
    }

    @PostMapping("/" + WebUrls.REGISTER)
    public String registerNewProject(Model model,
                                     @Valid @ModelAttribute("newProjectSession") NewProjectSession newProjectSession,
                                     BindingResult result) {
        if (result.hasErrors()) {
            return "new-project";
        }
        model.addAttribute("yandexOauthUrl", yandexService.getAuthorizationUrl());
        return "home";
    }

    @ModelAttribute("newProjectSession")
    public NewProjectSession newProjectSession() {
        return new NewProjectSession();
    }

}

