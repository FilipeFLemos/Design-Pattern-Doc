package models;

import utils.Utils;

import java.io.Serializable;
import java.util.*;

public class PatternInstance implements Serializable {

    private static final long serialVersionUID = 1L;

    private String patternName;
    private String intent;
    private Map<String, Set<String>> roleObjects;
    private Map<String, Set<String>> objectRoles;
    private Set<PatternParticipant> patternParticipants;

    public PatternInstance() {

    }

    public PatternInstance(String patternName, PatternCandidate patternCandidate) {
        this(patternName, "", patternCandidate.getRoles(), patternCandidate.getPatternParticipants());
    }

    public PatternInstance(PatternInstance patternInstance) {
        this(patternInstance.getPatternName(), patternInstance.getIntent(), patternInstance.roleObjects.keySet(), patternInstance.getPatternParticipants());
    }

    public PatternInstance(String patternName, String intent, Set<String> roles, Set<PatternParticipant> patternParticipants) {
        setPatternName(patternName);
        setIntent(intent);
        setRoleObjects(new HashMap<>());
        setObjectRoles(new HashMap<>());
        setPatternParticipants(new HashSet<>());

        setPatternRoles(roles);
        updatePatternParticipantsContainers(patternParticipants);
    }

    private void setPatternRoles(Set<String> roles) {
        for (String role : roles) {
            Set<String> objects = new HashSet<>();
            roleObjects.put(role, objects);
        }
    }

    public Set<String> getEmptyRoles(){
        Set<String> emptyRoles = new HashSet<>();
        for(Map.Entry<String, Set<String>> entry : roleObjects.entrySet()){
            String role = entry.getKey();
            Set<String> objects = entry.getValue();
            if(objects.isEmpty()){
                emptyRoles.add(role);
            }
        }
        return  emptyRoles;
    }

    public void updatePatternParticipantsContainers(Set<PatternParticipant> patternParticipants) {
        for (PatternParticipant patternParticipant : patternParticipants) {
            updateContainers(patternParticipant);
        }
    }

    private void updateContainers(PatternParticipant patternParticipant) {
        String role = patternParticipant.getRole();
        String object = patternParticipant.getObject();
        addRoleToObject(role, object);
        addObjectToRole(object, role);
        addPatternParticipant(role, object);
    }

    private void addRoleToObject(String role, String object) {
        Set<String> roles = new HashSet<>();
        if (objectRoles.containsKey(object)) {
            roles = objectRoles.get(object);
        }
        roles.add(role);
        objectRoles.put(object, roles);
    }

    private void addObjectToRole(String object, String role) {
        try {
            Set<String> objects = roleObjects.get(role);
            objects.add(object);
            roleObjects.put(role, objects);
        } catch (NullPointerException ignored) {

        }
    }

    private void addPatternParticipant(String role, String object) {
        this.patternParticipants.add(new PatternParticipant(object, role));
    }

    public void renameParticipantObject(String oldName, String newName) {
        updateRoleObjects(oldName, newName);
        updateObjectRoles(oldName, newName);
        updatePatternParticipants(oldName, newName);
    }

    private void updateRoleObjects(String oldName, String newName) {
        for (Map.Entry<String, Set<String>> entry : roleObjects.entrySet()) {
            Set<String> objects = entry.getValue();
            if (objects.contains(oldName)) {
                objects.remove(oldName);
                objects.add(newName);
                break;
            }
        }
    }

    private void updateObjectRoles(String oldName, String newName) {
        if (objectRoles.containsKey(oldName)) {
            Set<String> roles = objectRoles.get(oldName);
            objectRoles.remove(oldName);
            objectRoles.put(newName, roles);
        }

    }

    private void updatePatternParticipants(String oldName, String newName) {
        for (PatternParticipant patternParticipant : patternParticipants) {
            String object = patternParticipant.getObject();
            if (object.equals(oldName)) {
                patternParticipant.setObject(newName);
                break;
            }
        }
    }

    public boolean areTheSamePatternInstance(PatternInstance thatPatternInstance) {
        if(this.patternParticipants.size() == 1 || thatPatternInstance.patternParticipants.size() == 1){
            return false;
        }

        if (this.isSubSet(thatPatternInstance) || thatPatternInstance.isSubSet(this)) {
            return true;
        }

        if (!patternName.equals(thatPatternInstance.getPatternName())) {
            return false;
        }

        return hasEnoughParticipantsInCommon(thatPatternInstance);
    }

    private boolean isSubSet(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PatternInstance that = (PatternInstance) o;
        if (!patternName.equals(that.patternName)) return false;

        Set<PatternParticipant> thatPatternParticipants = that.getPatternParticipants();
        for (PatternParticipant thisPatternParticipant : patternParticipants) {
            if (!thatPatternParticipants.contains(thisPatternParticipant)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasEnoughParticipantsInCommon(PatternInstance thatPatternInstance) {
        int patternParticipantsInCommon = 0;
        Set<PatternParticipant> thatPatternParticipants = thatPatternInstance.getPatternParticipants();
        for (PatternParticipant thisPatternParticipant : patternParticipants) {
            if (thatPatternParticipants.contains(thisPatternParticipant)) {
                patternParticipantsInCommon++;
            }
        }
        return patternParticipantsInCommon >= Utils.MIN_PATTERN_PARTICIPANTS_IN_COMMON;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PatternInstance that = (PatternInstance) o;
        return Objects.equals(patternName, that.patternName) &&
                Objects.equals(roleObjects, that.roleObjects) &&
                Objects.equals(objectRoles, that.objectRoles) &&
                Objects.equals(patternParticipants, that.patternParticipants);
    }

    @Override
    public int hashCode() {
        return Objects.hash(patternName, roleObjects, objectRoles, patternParticipants);
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

    public void setIntent(String intent) {
        this.intent = intent;
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

    public Set<PatternParticipant> getPatternParticipants() {
        return patternParticipants;
    }

    public void setPatternParticipants(Set<PatternParticipant> patternParticipants) {
        this.patternParticipants = patternParticipants;
    }
}
