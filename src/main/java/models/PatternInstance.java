package models;

import java.io.Serializable;
import java.util.*;

public class PatternInstance implements Serializable {

    private static final long serialVersionUID = 1L;

    private String patternName;
    private String intent;
    private String collaborations;
    private Map<String, Set<String>> objectsByRole;

    public PatternInstance(String patternName, PatternCandidate patternCandidate) {
        this.patternName = patternName;
        objectsByRole = new HashMap<>();

        setIntent("");
        setCollaborations("");
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

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public void setCollaborations(String collaborations) {
        this.collaborations = collaborations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PatternInstance that = (PatternInstance) o;
        return Objects.equals(patternName, that.patternName) &&
                Objects.equals(objectsByRole, that.objectsByRole);
    }

    @Override
    public int hashCode() {
        return Objects.hash(patternName, intent, collaborations, objectsByRole);
    }
}
