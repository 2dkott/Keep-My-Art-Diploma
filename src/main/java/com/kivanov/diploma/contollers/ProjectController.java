package com.kivanov.diploma.contollers;

import com.kivanov.diploma.model.*;
import com.kivanov.diploma.services.*;
import com.kivanov.diploma.services.cloud.yandex.YandexService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/" + WebUrls.PROJECT)
@SessionAttributes({"newProjectSession","projectModel"})
public class ProjectController {

    @Autowired
    YandexService yandexService;

    @Autowired
    ProjectService projectService;

    @Autowired
    SourceService sourceService;



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
        if(!Files.exists(Path.of(newProjectSession.getLocalPath()))) {
            ObjectError error = new ObjectError("globalError", "Такой в путь в файловой системе не существует!");
            result.addError(error);
        }
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

        return "redirect:/";
    }

    @GetMapping("/" + WebUrls.DELETE + "/{projectId}")
    public String deleteProject(@PathVariable("projectId") long projectId) throws NoKeepProjectException {
        projectService.deleteProjectById(projectId);
        return "redirect:/";
    }

    @GetMapping("/" + WebUrls.SHOW + "/{projectId}")
    public String showProject(@PathVariable("projectId") long projectId,
                              @ModelAttribute("projectModel") KeepProjectModel projectModel,
                              ModelMap model) throws NoKeepProjectException {
        KeepProject project = projectService.findProjectById(projectId);
        List<KeepSource> sources = project.getKeepSources().stream().toList();
        SyncKeepFileData syncKeepFileData = (SyncKeepFileData) model.getAttribute("syncData");
        model.addAttribute("project", project);
        model.addAttribute("cloudSources", sources);
        projectModel.resetNewFileListWith(KeepFileModelMapper.mapToKeepFileModelList(syncKeepFileData.getNewCloudFiles()));
        projectModel.getNewFileList().addAll(KeepFileModelMapper.mapToKeepFileModelList(syncKeepFileData.getNewLocalFiles()));
        projectModel.resetModifiedNewFileList(KeepFileModelMapper.mapToKeepFileModifiedModelList(syncKeepFileData.getModifiedFiles()));
        model.addAttribute("projectModel", projectModel);
        return "project";
    }

    @GetMapping("/" + WebUrls.SYNC + "/{projectId}")
    public String syncProject(@PathVariable("projectId") long projectId,
                              RedirectAttributes redirectAttrs) throws NoKeepProjectException, FileDealingException {
        KeepProject project = projectService.findProjectById(projectId);
        redirectAttrs.addFlashAttribute("syncData", fileSyncService.syncLocalFiles(project));
        return "redirect:/" + WebUrls.PROJECT + "/" + WebUrls.SHOW + "/" + projectId;
    }

    @PostMapping("/" + WebUrls.UPLOAD + "/{projectId}")
    public String uploadProject(@PathVariable("projectId") long projectId,
                                @ModelAttribute("projectModel") KeepProjectModel projectModel) throws NoKeepProjectException, FileDealingException {
        KeepProject project = projectService.findProjectById(projectId);
        List<KeepFile> keepFiles = new ArrayList<>(projectModel.getNewFileList().stream()
                .filter(KeepFileModel::isChecked)
                .map(KeepFileModel::getFile)
                .filter(file -> !file.getSource().isCloud()).toList());
        keepFiles.addAll(
                projectModel.getModifiedNewFileList().stream()
                        .filter(KeepFileModifiedModel::isChecked)
                        .map(KeepFileModifiedModel::getLeftFile)
                        .filter(file -> !file.getSource().isCloud()).toList()
        );
        fileSyncService.uploadFiles(keepFiles, project.getCloudSource());
        return "redirect:/" + WebUrls.PROJECT + "/" + WebUrls.SHOW + "/" + projectId;
    }

    @PostMapping("/" + WebUrls.DOWNLOAD + "/{projectId}")
    public String downloadProject(@PathVariable("projectId") long projectId,
                                @ModelAttribute("projectModel") KeepProjectModel projectModel) throws NoKeepProjectException {
        KeepProject project = projectService.findProjectById(projectId);
        List<KeepFile> keepFiles = new ArrayList<>(projectModel.getNewFileList().stream()
                .filter(KeepFileModel::isChecked)
                .map(KeepFileModel::getFile)
                .filter(file -> file.getSource().isCloud()).toList());
        keepFiles.addAll(
                projectModel.getModifiedNewFileList().stream()
                .filter(KeepFileModifiedModel::isChecked)
                .map(KeepFileModifiedModel::getRightFile)
                .filter(file -> file.getSource().isCloud()).toList()
        );
        if(!keepFiles.isEmpty()) fileSyncService.downloadFiles(keepFiles, project.getLocalSource());
        return "redirect:/" + WebUrls.PROJECT + "/" + WebUrls.SHOW + "/" + projectId;
    }

    @ModelAttribute("newProjectSession")
    public NewProjectSession newProjectSession() {
        return new NewProjectSession();
    }

    @ModelAttribute("projectModel")
    public KeepProjectModel projectModel() {
        return new KeepProjectModel();
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

