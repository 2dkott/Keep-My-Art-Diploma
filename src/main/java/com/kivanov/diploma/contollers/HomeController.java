package com.kivanov.diploma.contollers;

import com.kivanov.diploma.model.WebUrls;
import com.kivanov.diploma.services.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("")
public class HomeController {

    @Autowired
    ProjectService projectService;

    @GetMapping
    public String home(Model model) {
        model.addAttribute("projects", projectService.getAllKeepProjects());
        return "home";
    }

    @ModelAttribute("urls")
    public WebUrls urls() {
        return new WebUrls();
    }
}
