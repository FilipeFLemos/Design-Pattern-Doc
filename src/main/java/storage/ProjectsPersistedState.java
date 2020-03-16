package storage;

import java.util.concurrent.ConcurrentHashMap;

public class ProjectsPersistedState {

    private ConcurrentHashMap<String, ProjectPersistedState> persistedStateByProject = new ConcurrentHashMap<>();

    public ProjectsPersistedState(){}

    public boolean containsProject(String projectName){
        return persistedStateByProject.containsKey(projectName);
    }

    public ProjectPersistedState getProjectState(String projectName){
        return persistedStateByProject.get(projectName);
    }

    public void putProjectState(String projectName){
        persistedStateByProject.put(projectName, new ProjectPersistedState());
    }

    public void setPersistedStateByProject(ConcurrentHashMap<String, ProjectPersistedState> persistedStateByProject) {
        this.persistedStateByProject = persistedStateByProject;
    }

    public ConcurrentHashMap<String, ProjectPersistedState> getPersistedStateByProject() {
        return persistedStateByProject;
    }
}
