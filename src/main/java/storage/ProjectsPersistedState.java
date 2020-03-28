package storage;

import models.DesignPattern;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ProjectsPersistedState {

    private ConcurrentHashMap<String, ProjectPersistedState> projectsPersistedStateByProjectName = new ConcurrentHashMap<>();
    private Set<DesignPattern> supportedDesignPatterns = new HashSet<>();

    public ProjectsPersistedState(){
    }

    public boolean containsProject(String projectName){
        return projectsPersistedStateByProjectName.containsKey(projectName);
    }

    public ProjectPersistedState getProjectState(String projectName){
        return projectsPersistedStateByProjectName.get(projectName);
    }

    public void putProjectState(String projectName){
        projectsPersistedStateByProjectName.put(projectName, new ProjectPersistedState());
    }

    public ConcurrentHashMap<String, ProjectPersistedState> getProjectsPersistedStateByProjectName() {
        return projectsPersistedStateByProjectName;
    }

    public void setProjectsPersistedStateByProjectName(ConcurrentHashMap<String, ProjectPersistedState> projectsPersistedStateByProjectName) {
        this.projectsPersistedStateByProjectName = projectsPersistedStateByProjectName;
    }

    public Set<DesignPattern> getSupportedDesignPatterns() {
        return supportedDesignPatterns;
    }

    public boolean areSupportDesignPatternsNotInitialized(){
        return supportedDesignPatterns.isEmpty();
    }

    public void setSupportedDesignPatterns(Set<DesignPattern> supportedDesignPatterns) {
        this.supportedDesignPatterns = supportedDesignPatterns;
    }

    public void storeDesignPattern(DesignPattern designPattern){
        supportedDesignPatterns.add(designPattern);
    }
}
