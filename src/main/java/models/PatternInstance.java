package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PatternInstance {

    private String patternName;
    private Map<String, ArrayList<String>> objectsByRole;

    public PatternInstance(String patternName, PatternCandidate patternCandidate){
        this.patternName = patternName;
        objectsByRole = new HashMap<>();

        setPatternRoles(patternCandidate);
    }

    private void setPatternRoles(PatternCandidate patternCandidate){
        for (Map.Entry<String, String> entry : patternCandidate.getObjectByRole().entrySet())
        {
            String role = entry.getKey();
            ArrayList<String> objects = new ArrayList<>();
            objects.add(entry.getValue());
            objectsByRole.put(role,objects);
        }
    }

    public void addObjectToRole(PatternCandidate patternCandidate){
        for (Map.Entry<String, String> entry : patternCandidate.getObjectByRole().entrySet())
        {
            String role = entry.getKey();
            ArrayList<String> objects = objectsByRole.get(role);

            if(objects == null)
            {
                return;
            }

            objects.add(entry.getValue());
            objectsByRole.put(role,objects);
        }
    }
}
