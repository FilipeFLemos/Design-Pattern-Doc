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
        setPatternParticipant(patternCandidate);
    }

    private void setPatternRoles(PatternCandidate patternCandidate) {
        Set<PatternParticipant> patternParticipants = patternCandidate.getPatternParticipants();
        for (PatternParticipant patternParticipant : patternParticipants) {
            String role = patternParticipant.getRole();
            Set<String> objects = new HashSet<>();
            roleObjects.put(role, objects);
        }
    }

    private void setPatternParticipant(PatternCandidate patternCandidate) {
        Set<PatternParticipant> patternParticipants = patternCandidate.getPatternParticipants();
        for (PatternParticipant patternParticipant : patternParticipants) {
            String role = patternParticipant.getRole();
            String object = patternParticipant.getObject();
            addObjectToRole(object, role);
            addRoleToObject(role, object);
        }
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
                addRoleToObject(role, object);
                addObjectToRole(object, role);
            }
        }
    }

    private void addRoleToObject(String role, String object){
        Set<String> roles = objectRoles.get(object);
        if(roles == null){
            roles = new HashSet<>();
        }
        roles.add(role);

        objectRoles.put(object,roles);
    }

    private void addObjectToRole(String object, String role){
        Set<String> objects = roleObjects.get(role);
        if(objects == null){
            objects = new HashSet<>();
        }
        objects.add(object);

        roleObjects.put(role,objects);
    }

    public void addPatternParticipant(PatternCandidate patternCandidate) {
        Set<PatternParticipant> patternParticipants = patternCandidate.getPatternParticipants();
        for (PatternParticipant patternParticipant : patternParticipants) {
            String role = patternParticipant.getRole();
            if(!roleObjects.containsKey(role)){
                return;
            }

            Set<String> objects = roleObjects.get(role);
            String object = patternParticipant.getObject();

            objects.add(object);
            roleObjects.put(role, objects);
            addRoleToObject(role, object);
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
