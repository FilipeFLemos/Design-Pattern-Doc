package storage;

import models.PatternInstance;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bias
 */
public class PersistentState{

    public ConcurrentHashMap<String, PatternInstance> patternInstanceById = new ConcurrentHashMap<>();

    public PersistentState(){}

    public ConcurrentHashMap<String, PatternInstance> getPatternInstanceById() {
        return patternInstanceById;
    }

    public PatternInstance getPatternInstance(String id){
        return patternInstanceById.get(id);
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
