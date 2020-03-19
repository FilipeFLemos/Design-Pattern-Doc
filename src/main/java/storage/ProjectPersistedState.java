package storage;

import models.PatternInstance;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProjectPersistedState implements Serializable {

    private static final long serialVersionUID = 1L;

    public ConcurrentHashMap<String, PatternInstance> patternInstanceById = new ConcurrentHashMap<>();

    public ProjectPersistedState(){}

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
            if(patternInstance.areTheSamePatternInstance(storedInstance)){
                hasAlreadyStored = true;
                break;
            }
        }

        return hasAlreadyStored;
    }

    public String getPatternInstanceId(PatternInstance patternInstance){
        String id = "";
        for (Map.Entry<String, PatternInstance> entry : patternInstanceById.entrySet()) {
            PatternInstance storedInstance = entry.getValue();
            if(patternInstance.areTheSamePatternInstance(storedInstance)){
                id = entry.getKey();
                break;
            }
        }
        return id;
    }
}
