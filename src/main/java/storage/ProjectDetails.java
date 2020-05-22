package storage;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import models.PatternInstance;
import utils.Utils;

import java.awt.*;
import java.util.*;

public class ProjectDetails {

    private String name;
    private String path;
    private ProjectPersistedState persistedState;
    private Project activeProject;

    public ProjectDetails() {
        try {
            Project activeProject = getActiveProject();
            String activeProjectName = activeProject.getName();
            String activeProjectPath = activeProject.getBasePath();
            setName(activeProjectName);
            setPath(activeProjectPath);
        } catch (Exception e) {
            setName("");
            setPath("");
        }
    }

    public ProjectPersistedState getActiveProjectPersistedState(){
        activeProject = getActiveProject();
        String activeProjectName = activeProject.getName();
        if (!existsProjectState() || !name.equals(activeProjectName)) {
            setActiveProject(activeProject);
        }
        return persistedState;
    }

    public void updateProjectPersistedState(PatternInstance patternInstance) {
        String id = persistedState.getPatternInstanceId(patternInstance);
        if (isPatternInstanceNotStored(id)) {
            id = Utils.generatePatternInstanceId(persistedState.getPatternInstanceById());
        }
        persistedState.updatePatternInstance(id, patternInstance);
    }

    private boolean isPatternInstanceNotStored(String id) {
        return id.equals("");
    }

    public String getPath() {
        return path;
    }

    private boolean existsProjectState() {
        return persistedState != null;
    }

    private void setActiveProject(Project activeProject) {
        try {
            String activeProjectName = activeProject.getName();
            String activeProjectPath = activeProject.getBasePath();
            setName(activeProjectName);
            setPath(activeProjectPath);
            setPersistedState();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setPersistedState() throws NullPointerException {
        ProjectsPersistedState projectsPersistedState = PluginState.getInstance().getState();
        if (projectsPersistedState == null) {
            throw new NullPointerException();
        }

        if (!projectsPersistedState.containsProject(name)) {
            projectsPersistedState.putProjectState(name);
        }
        persistedState = projectsPersistedState.getProjectState(name);
    }

    public Project getActiveProject() {
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        for (Project project : projects) {
            Window window = WindowManager.getInstance().suggestParentWindow(project);
            if (window != null && window.isActive()) {
                activeProject = project;
                break;
            }
        }
        return activeProject;
    }

    public Set<String> getAllFileNamesFromProject() {
        activeProject = getActiveProject();
        Set<String> filesName = new HashSet<>();
        if(activeProject.isDisposed()){
            return new HashSet<>();
        }

        Collection<VirtualFile> files = Utils.getVirtualFilesInProject(activeProject);

        for (VirtualFile virtualFile : files) {
            String fileName = virtualFile.getName();
            String[] parsedFileName = fileName.split(".java");
            filesName.add(parsedFileName[0]);
        }
        return filesName;
    }

    private void setName(String name) {
        this.name = name;
    }

    private void setPath(String path) {
        this.path = path;
    }
}
