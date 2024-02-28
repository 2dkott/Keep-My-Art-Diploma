package com.kivanov.diploma.contollers;

import com.kivanov.diploma.model.KeepProject;
import com.kivanov.diploma.model.KeepSource;
import com.kivanov.diploma.model.WebUrls;
import com.kivanov.diploma.services.NoKeepSourceException;
import com.kivanov.diploma.services.ProjectService;
import com.kivanov.diploma.services.SourceService;
import com.kivanov.diploma.services.YandexService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/" + WebUrls.PROJECT)
@SessionAttributes({"newProjectSession"})
public class ProjectController {

    @Autowired
    YandexService yandexService;

    @Autowired
    ProjectService projectService;

    @Autowired
    SourceService sourceService;

    @GetMapping("/" + WebUrls.NEW)
    public String showNewProject(Model model, @ModelAttribute("newProjectSession") NewProjectSession newProjectSession) {
        model.addAttribute("yandexOauthUrl", yandexService.getAuthorizationUrl());
        return "new-project";
    }

    @PostMapping(WebUrls.DELETE_SOURCE_FROM_NEW + "/{userName}")
    public String removeSourceById(@PathVariable("userName") String userName,
                                   @ModelAttribute("newProjectSession") NewProjectSession newProjectSession,
                                   Model model) {
        Optional<KeepSource> sourceToDelete = newProjectSession.getKeepSourceList().stream()
                .filter(keepSource -> keepSource.getUserName().equals(userName)).findAny();
        try {
            sourceToDelete.map(source -> newProjectSession.getKeepSourceList().remove(source)).orElseThrow(() -> new NoKeepSourceException(userName));
        } catch (Exception e) {
            model.addAttribute("error-message", e.getMessage());
            return "error";
        }
        return "new-project";
    }


    @PostMapping("/" + WebUrls.REGISTER)
    public String registerNewProject(Model model,
                                     @Valid @ModelAttribute("newProjectSession") NewProjectSession newProjectSession,
                                     BindingResult result) {
        if (result.hasErrors()) {
            return "new-project";
        }

        KeepProject keepProject = new KeepProject();
        keepProject.setName(newProjectSession.getProjectName());

        projectService.createProject(keepProject);

        KeepSource mainSource = new KeepSource();
        mainSource.setPath(newProjectSession.getLocalPath());

        List<KeepSource> keepSources = newProjectSession.getKeepSourceList();
        keepSources.add(mainSource);

        keepSources.forEach(keepSource -> keepSource.setProject(keepProject));

        sourceService.createAll(keepSources);

        return "home";
    }

    @ModelAttribute("newProjectSession")
    public NewProjectSession newProjectSession() {
        return new NewProjectSession();
    }

    @ModelAttribute("urls")
    public WebUrls urls() {
        return new WebUrls();
    }

}

