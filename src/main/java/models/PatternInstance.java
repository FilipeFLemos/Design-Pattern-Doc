package models;

import java.util.*;

public class PatternInstance {

    private String patternName;
    private Map<String, Set<String>> objectsByRole;

    public PatternInstance(String patternName, PatternCandidate patternCandidate) {
        this.patternName = patternName;
        objectsByRole = new HashMap<>();

        setPatternRoles(patternCandidate);
    }

    private void setPatternRoles(PatternCandidate patternCandidate) {
        for (Map.Entry<String, String> entry : patternCandidate.getObjectByRole().entrySet()) {
            String role = entry.getKey();
            Set<String> objects = new HashSet<>();
            objects.add(entry.getValue());
            objectsByRole.put(role, objects);
        }
    }

    public void addObjectToRole(PatternCandidate patternCandidate) {
        for (Map.Entry<String, String> entry : patternCandidate.getObjectByRole().entrySet()) {
            String role = entry.getKey();
            Set<String> objects = objectsByRole.get(role);

            if (objects == null) {
                return;
            }

            objects.add(entry.getValue());
            objectsByRole.put(role, objects);
        }
    }
}
