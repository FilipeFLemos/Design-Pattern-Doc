package storage;

import java.util.concurrent.ConcurrentHashMap;

public class PersistentState{

    private ConcurrentHashMap<String, ProjectState> persistedStateByProject = new ConcurrentHashMap<>();

    public PersistentState(){}

    public boolean containsProject(String projectName){
        return persistedStateByProject.containsKey(projectName);
    }

    public ProjectState getProjectState(String projectName){
        return persistedStateByProject.get(projectName);
    }

    public void putProjectState(String projectName){
        persistedStateByProject.put(projectName, new ProjectState());
    }

    public void setPersistedStateByProject(ConcurrentHashMap<String, ProjectState> persistedStateByProject) {
        this.persistedStateByProject = persistedStateByProject;
    }

    public ConcurrentHashMap<String, ProjectState> getPersistedStateByProject() {
        return persistedStateByProject;
    }
}
