package models;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
        if(!isAlreadyStored(patternInstance)){
            patternInstanceById.putIfAbsent(id, patternInstance);
        }
    }

    public boolean isAlreadyStored(PatternInstance patternInstance){
        boolean isAlreadyStored = false;

        for (Map.Entry<String, PatternInstance> entry : patternInstanceById.entrySet()) {
            PatternInstance storedInstance = entry.getValue();
            if(patternInstance.equals(storedInstance)){
                isAlreadyStored = true;
                break;
            }
        }

        return isAlreadyStored;
    }
}
