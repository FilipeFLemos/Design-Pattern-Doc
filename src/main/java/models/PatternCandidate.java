package models;

import java.util.Map;

public class PatternCandidate {

    private Map<String, String> objectByRole;

    public PatternCandidate(Map<String, String> objectByRole){
        this.objectByRole = objectByRole;
    }

    public Map<String, String> getObjectByRole() {
        return objectByRole;
    }
}
