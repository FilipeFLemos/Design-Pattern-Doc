package storage;

import models.PatternInstance;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProjectState implements Serializable {

    private static final long serialVersionUID = 1L;

    public ConcurrentHashMap<String, PatternInstance> patternInstanceById = new ConcurrentHashMap<>();

    public ProjectState(){}

    public ConcurrentHashMap<String, PatternInstance> getPatternInstanceById() {
        return patternInstanceById;
    }

    public PatternInstance getPatternInstance(String id){
        return patternInstanceById.get(id);
    }

    public void deletePatternInstance(String id){
        patternInstanceById.remove(id);
    }

    public void updatePatternInstance(String id, PatternInstance patternInstance){
        patternInstanceById.put(id, patternInstance);
    }

    public void storePatternInstanceIfAbsent(String id, PatternInstance patternInstance){
        if(!hasAlreadyStored(patternInstance)){
            patternInstanceById.putIfAbsent(id, patternInstance);
        }
    }

    public boolean hasAlreadyStored(PatternInstance patternInstance){
        boolean hasAlreadyStored = false;

        for (Map.Entry<String, PatternInstance> entry : patternInstanceById.entrySet()) {
            PatternInstance storedInstance = entry.getValue();
            if(patternInstance.equals(storedInstance)){
                hasAlreadyStored = true;
                break;
            }
        }

        return hasAlreadyStored;
    }
}
