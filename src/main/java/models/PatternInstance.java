package models;

import java.io.Serializable;
import java.util.*;

public class PatternInstance implements Serializable {

    private static final long serialVersionUID = 1L;

    private String patternName;
    private String intent;
    private String collaborations;
    private Map<String, Set<String>> roleObjects;
    private Map<String, Set<String>> objectRoles;

    private boolean isAnHint = false;

    public PatternInstance(){

    }

    public PatternInstance(String patternName, PatternCandidate patternCandidate) {
        roleObjects = new HashMap<>();
        objectRoles = new HashMap<>();

        setPatternName(patternName);
        setIntent("");
        setCollaborations("");
        setPatternRoles(patternCandidate);
    }

    private void setPatternRoles(PatternCandidate patternCandidate) {
        for (Map.Entry<String, String> entry : patternCandidate.getObjectByRole().entrySet()) {
            String role = entry.getKey();
            Set<String> objects = new HashSet<>();
            objects.add(entry.getValue());
            roleObjects.put(role, objects);

            for(String object : objects){
                addRoleToObject(object, role);
            }
        }
    }

    private void addRoleToObject(String object, String role){
        Set<String> roles = objectRoles.get(object);
        if(roles == null){
            roles = new HashSet<>();
        }
        roles.add(role);

        objectRoles.put(object,roles);
    }

    public void addObjectToRole(PatternCandidate patternCandidate) {
        for (Map.Entry<String, String> entry : patternCandidate.getObjectByRole().entrySet()) {
            String role = entry.getKey();
            Set<String> objects = roleObjects.get(role);

            if (objects == null) {
                return;
            }

            objects.add(entry.getValue());
            roleObjects.put(role, objects);
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
                Objects.equals(roleObjects, that.roleObjects);
    }

    @Override
    public int hashCode() {
        return Objects.hash(patternName, intent, collaborations, roleObjects);
    }

    public String getPatternName() {
        return patternName;
    }

    public void setPatternName(String patternName) {
        this.patternName = patternName;
    }

    public String getIntent() {
        return intent;
    }

    public String getCollaborations() {
        return collaborations;
    }

    public Map<String, Set<String>> getRoleObjects() {
        return roleObjects;
    }

    public void setRoleObjects(Map<String, Set<String>> roleObjects) {
        this.roleObjects = roleObjects;
    }

    public Map<String, Set<String>> getObjectRoles() {
        return objectRoles;
    }

    public void setObjectRoles(Map<String, Set<String>> objectRoles) {
        this.objectRoles = objectRoles;
    }

    public boolean isAnHint() {
        return isAnHint;
    }

    public void setAnHint(boolean anHint) {
        isAnHint = anHint;
    }
}
