package models;

import java.io.Serializable;
import java.util.*;

public class PatternInstance implements Serializable{

    private static final long serialVersionUID = 1L;

    private String patternName;
    private String intent;
    private Map<String, Set<String>> roleObjects;
    private Map<String, Set<String>> objectRoles;

    public PatternInstance(){

    }

    public PatternInstance(String patternName, PatternCandidate patternCandidate) {
        roleObjects = new HashMap<>();
        objectRoles = new HashMap<>();

        setPatternName(patternName);
        setIntent("");
        setPatternRoles(patternCandidate);
    }

    public PatternInstance(String name, String intent, Map<String, Set<String>> roleObjects, Map<String, Set<String>> objectRoles) {
        setPatternName(name);
        setIntent(intent);
        setRoleObjects(roleObjects);
        setObjectRoles(objectRoles);
    }

    public PatternInstance(PatternInstance patternInstance){
        this(patternInstance.getPatternName(), patternInstance.getIntent(), new HashMap<>(), new HashMap<>());
        mergePatternParticipants(patternInstance);
    }

    public void mergePatternParticipants(PatternInstance newPatternInstance){
        Map<String, Set<String>> objectRoles = newPatternInstance.getObjectRoles();
        for (Map.Entry<String, Set<String>> entry : objectRoles.entrySet()) {
            String object = entry.getKey();
            Set<String> roles = entry.getValue();
            for(String role : roles) {
                addRoleToObject(object, role);
                addObjectToRole(role, object);
            }
        }
    }

    public PatternInstance(PatternInstance oldPatternInstance, PatternInstance newPatternInstance){
        this.patternName = oldPatternInstance.getPatternName();
        this.intent = oldPatternInstance.getIntent();
        this.roleObjects = oldPatternInstance.getRoleObjects();
        this.objectRoles = oldPatternInstance.getObjectRoles();
        Map<String, Set<String>> objectRoles = newPatternInstance.getObjectRoles();
        for (Map.Entry<String, Set<String>> entry : objectRoles.entrySet()) {
            String object = entry.getKey();
            Set<String> roles = entry.getValue();
            for(String role : roles) {
                addRoleToObject(object, role);
                addObjectToRole(role, object);
            }
        }
    }

    private void setPatternRoles(PatternCandidate patternCandidate) {
        for (Map.Entry<String, String> entry : patternCandidate.getObjectByRole().entrySet()) {
            String role = entry.getKey();
            Set<String> objects = new HashSet<>();
            String object = entry.getValue();

            objects.add(object);
            roleObjects.put(role, objects);
            addRoleToObject(object,role);
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

    private void addObjectToRole(String role, String object){
        Set<String> objects = roleObjects.get(role);
        if(objects == null){
            objects = new HashSet<>();
        }
        objects.add(object);

        roleObjects.put(role,objects);
    }

    public void addObjectToRole(PatternCandidate patternCandidate) {
        for (Map.Entry<String, String> entry : patternCandidate.getObjectByRole().entrySet()) {
            String role = entry.getKey();
            String object = entry.getValue();
            Set<String> objects = roleObjects.get(role);

            if (objects == null) {
                return;
            }

            objects.add(object);
            roleObjects.put(role, objects);
            addRoleToObject(object,role);
        }
    }

    public ArrayList<PatternParticipant> getCollaborationRows(){
        ArrayList<PatternParticipant> collaborationRows = new ArrayList<>();

        for (Map.Entry<String, Set<String>> entry : objectRoles.entrySet()) {
            String className = entry.getKey();
            Set<String> roles = entry.getValue();

            for(String role : roles){
                PatternParticipant patternParticipant = new PatternParticipant(className, role);
                collaborationRows.add(patternParticipant);
            }
        }

        return collaborationRows;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public boolean areTheSamePatternInstance(PatternInstance thatPatternInstance) {
        if (this.isSubSet(thatPatternInstance) || thatPatternInstance.isSubSet(this)) {
            return true;
        }

        //TODO threshold

        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PatternInstance that = (PatternInstance) o;
        return Objects.equals(patternName, that.patternName) &&
                Objects.equals(roleObjects, that.roleObjects) &&
                Objects.equals(objectRoles, that.objectRoles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(patternName, intent, roleObjects, objectRoles);
    }

    public boolean isSubSet(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PatternInstance that = (PatternInstance) o;
        if(!patternName.equals(that.patternName)) return false;

        ArrayList<PatternParticipant> thisCollaborationRows = getCollaborationRows();
        ArrayList<PatternParticipant> thatCollaborationRows = that.getCollaborationRows();
        for(PatternParticipant thisPatternParticipant : thisCollaborationRows){
            if(!thatCollaborationRows.contains(thisPatternParticipant)){
                return false;
            }
        }
        return true;
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
}
