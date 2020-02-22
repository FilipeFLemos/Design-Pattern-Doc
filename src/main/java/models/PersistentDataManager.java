package models;

import utils.Utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PersistentDataManager {

    private ConcurrentHashMap<String, PatternInstance> patternInstanceById;
    private final String fileName = "patternInstances";

    private static PersistentDataManager instance = null;

    private PersistentDataManager()
    {
        patternInstanceById = (ConcurrentHashMap) Utils.loadObject(fileName);

        if(patternInstanceById == null){
            patternInstanceById = new ConcurrentHashMap<>();
        }
    }

    public static PersistentDataManager getInstance()
    {
        if (instance == null) {
            instance = new PersistentDataManager();
        }

        return instance;
    }

    public PatternInstance getPatternInstance(String id){
        return patternInstanceById.get(id);
    }

    public void updatePatternInstance(String id, PatternInstance patternInstance){
        patternInstanceById.put(id, patternInstance);
        Utils.saveObject(fileName, patternInstanceById);
    }

    public void addPatternInstance(String id, PatternInstance patternInstance){
        if(!isAlreadyStored(patternInstance)){
            patternInstanceById.putIfAbsent(id, patternInstance);
            Utils.saveObject(fileName, patternInstanceById);
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
