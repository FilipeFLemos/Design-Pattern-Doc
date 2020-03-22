package storage;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.WindowManager;
import models.PatternInstance;
import utils.Utils;

import java.awt.*;

public class ProjectDetails {

    private String name;
    private String path;
    private ProjectPersistedState persistedState;

    public ProjectDetails(){
        try {
            Project activeProject = getActiveProject();
            String activeProjectName = activeProject.getName();
            String activeProjectPath = activeProject.getBasePath();
            setName(activeProjectName);
            setPath(activeProjectPath);
        }catch (Exception e){
            setName("");
            setPath("");
        }
    }

    public ProjectPersistedState getActiveProjectPersistedState(){
        Project activeProject = getActiveProject();
        String activeProjectName = activeProject.getName();
        if (!existsProjectState() || !name.equals(activeProjectName)) {
            setActiveProject(activeProject);
        }
        return persistedState;
    }

    public void updateProjectPersistedState(PatternInstance patternInstance){
        String id = persistedState.getPatternInstanceId(patternInstance);
        if(isPatternInstanceNotStored(id)){
            id = Utils.generatePatternInstanceId(persistedState.getPatternInstanceById());
        }
        persistedState.updatePatternInstance(id, patternInstance);
    }

    private boolean isPatternInstanceNotStored(String id){
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
        }catch (Exception ignored){

        }
    }

    private void setPersistedState() throws NullPointerException{
        ProjectsPersistedState projectsPersistedState = PluginState.getInstance().getState();
        if(projectsPersistedState == null){
            throw new NullPointerException();
        }

        if (!projectsPersistedState.containsProject(name)) {
            projectsPersistedState.putProjectState(name);
        }
        persistedState = projectsPersistedState.getProjectState(name);
    }

    public Project getActiveProject() {
        Project activeProject = null;
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

    private void setName(String name) {
        this.name = name;
    }

    private void setPath(String path) {
        this.path = path;
    }

}
