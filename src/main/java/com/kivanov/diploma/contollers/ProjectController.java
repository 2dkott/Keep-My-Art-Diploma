package com.kivanov.diploma.contollers;

import com.kivanov.diploma.model.KeepProject;
import com.kivanov.diploma.model.KeepSource;
import com.kivanov.diploma.model.WebUrls;
import com.kivanov.diploma.services.YandexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/" + WebUrls.PROJECT)
@SessionAttributes({"keepSources", "project", "localSource"})
public class ProjectController {

    @Autowired
    YandexService yandexService;

    @GetMapping("/" + WebUrls.NEW)
    public String showNewProject(Model model,
                                @ModelAttribute("keepSources") List<KeepSource> keepSources,
                                @ModelAttribute("project") KeepProject project,
                                @ModelAttribute("localSource") KeepSource localSource) {
        model.addAttribute("yandexOauthUrl", yandexService.getAuthorizationUrl());
        return "new-project";
    }

    @PostMapping("/" + WebUrls.REGISTER)
    public String registerNewProject(Model model,
                                @ModelAttribute("keepSources") List<KeepSource> keepSources,
                                @ModelAttribute("project") KeepProject project,
                                @ModelAttribute("localSource") KeepSource localSource) {
        model.addAttribute("yandexOauthUrl", yandexService.getAuthorizationUrl());
        return "new-project";
    }


    @ModelAttribute("keepSources")
    public List<KeepSource> keepSources() {
        return new ArrayList<>();
    }

    @ModelAttribute("project")
    public KeepProject project() {
        return new KeepProject();
    }

    @ModelAttribute("localSource")
    public KeepSource localSource() {
        return new KeepSource();
    }
}

