package com.kivanov.diploma.contollers;

import com.kivanov.diploma.model.*;
import com.kivanov.diploma.services.*;
import com.kivanov.diploma.services.cloud.yandex.YandexService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.util.ArrayList;
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

    @Autowired
    KeepFileModelMapper keepFileModelMapper;

    @Autowired
    FileRepositoryService fileRepositoryService;

    @Autowired
    FileSyncService fileSyncService;

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

        projectService.saveProject(keepProject);

        KeepSource localSource = new KeepSource();
        localSource.setType(SourceType.LOCAL);
        localSource.setPath(newProjectSession.getLocalPath());
        localSource.setClone(false);

        List<KeepSource> keepSources = newProjectSession.getKeepSourceList();
        keepSources.add(localSource);

        keepSources.forEach(keepSource -> keepSource.setProject(keepProject));

        sourceService.saveKeepSources(keepSources);

        return "home";
    }

    @GetMapping("/" + WebUrls.SHOW + "/{projectId}")
    public String showProject(@PathVariable("projectId") long projectId,
                              Model model) throws NoKeepProjectException {
        KeepProject project = projectService.findProjectById(projectId);
        List<KeepSource> sources = project.getKeepSources().stream().toList();
        SyncKeepFileData syncKeepFileData = (SyncKeepFileData) model.getAttribute("syncData");
        model.addAttribute("project", project);
        model.addAttribute("cloudSources", sources);
        model.addAttribute("files", keepFileModelMapper.getFileList(fileRepositoryService.getProjectOnlyFiles(project)));
        //assert syncKeepFileData != null;
        List<KeepFile> allList = new ArrayList<>();
        allList.addAll(syncKeepFileData.getNewLocalFiles());
        allList.addAll(syncKeepFileData.getNewCloudFiles());
        model.addAttribute("allFiles", keepFileModelMapper.getFileList(allList));
        model.addAttribute("newLocalFiles", keepFileModelMapper.getFileList(syncKeepFileData.getNewLocalFiles()));
        model.addAttribute("newCloudFiles", keepFileModelMapper.getFileList(syncKeepFileData.getNewCloudFiles()));
        return "project";
    }

    @GetMapping("/" + WebUrls.SYNC + "/{projectId}")
    public String syncProject(@PathVariable("projectId") long projectId,
                              RedirectAttributes redirectAttrs) throws NoKeepProjectException, FileDealingException {
        KeepProject project = projectService.findProjectById(projectId);
        //fileSyncService.syncLocalFiles(project);
        //model.addAttribute("syncData", fileSyncService.syncLocalFiles(project));
        //syncData = fileSyncService.syncLocalFiles(project);
        redirectAttrs.addFlashAttribute("syncData", fileSyncService.syncLocalFiles(project));
        return "redirect:/" + WebUrls.PROJECT + "/" + WebUrls.SHOW + "/" + projectId;
        //return new RedirectView("/" + WebUrls.PROJECT + "/" + WebUrls.SHOW + "/" + projectId);
    }

    @ModelAttribute("newProjectSession")
    public NewProjectSession newProjectSession() {
        return new NewProjectSession();
    }


    @ModelAttribute("urls")
    public WebUrls urls() {
        return new WebUrls();
    }
    @ModelAttribute("syncData")
    public SyncKeepFileData syncData() {
        return new SyncKeepFileData();
    }

}

