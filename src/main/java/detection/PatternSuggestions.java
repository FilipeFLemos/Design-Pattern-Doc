package detection;

import models.PatternInstance;
import models.PatternParticipant;
import storage.PluginState;
import storage.ProjectPersistedState;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PatternSuggestions {
    private Map<String, Set<PatternInstance>> acceptedSuggestions;
    private Map<String, Set<PatternInstance>> availableSuggestions;

    private String object;
    private String role;

    public PatternSuggestions() {
        acceptedSuggestions = new HashMap<>();
        availableSuggestions = new HashMap<>();
    }

    public Map<String, Set<PatternInstance>> getAvailableSuggestions() {
        return availableSuggestions;
    }

    void updateSuggestions(Set<PatternInstance> patternInstances) {
        for (PatternInstance patternInstance : patternInstances) {
            ArrayList<PatternParticipant> patternParticipants = patternInstance.getCollaborationRows();
            for (PatternParticipant patternParticipant : patternParticipants) {
                object = patternParticipant.getObject();
                role = patternParticipant.getRole();

                if (availableSuggestions.containsKey(object)) {
                    try{
                        PatternInstance patternInstanceFound = getPatternInstanceInSuggestionMap(patternInstance, availableSuggestions);
                        if (!patternInstance.equals(patternInstanceFound)) {
                            replaceAvailableSuggestions(patternInstance, patternInstanceFound);
                        }
                        continue;
                    }catch (NullPointerException ignored){

                    }
                }

                if (acceptedSuggestions.containsKey(object)) {
                    try{
                        PatternInstance patternInstanceFound = getPatternInstanceInSuggestionMap(patternInstance, acceptedSuggestions);
                        Set<String> roles = patternInstanceFound.getObjectRoles().get(object);
                        if (!roles.contains(role)) {
                            PatternInstance mergedPatternInstance = new PatternInstance(patternInstanceFound, patternInstance);
                            moveAcceptedPatternInstanceToAvailable(object, mergedPatternInstance);
                        }
                        else if(!patternInstance.equals(patternInstanceFound)){
                            patternInstanceFound.mergePatternParticipants(patternInstance);
                            //replaceAcceptedSuggestions(patternInstance, patternInstanceFound);
                        }
                        continue;
                    }catch (NullPointerException ignored){

                    }
                }

                try{
                    PatternInstance patternInstanceFound = getPatternInstanceInPersistentStorage(patternInstance);
                    Set<String> roles = patternInstanceFound.getObjectRoles().get(object);
                    PatternInstance copyPatternInstance = new PatternInstance(patternInstanceFound);
                    copyPatternInstance.mergePatternParticipants(patternInstance);
                    if (roles == null || !roles.contains(role)) {
                        addAvailableSuggestion(object, copyPatternInstance);
                    }
                    else if(roles.contains(role)){
                        addAcceptedSuggestion(object, copyPatternInstance);
                    }
                    continue;
                }catch (NullPointerException ignored){
                }

                addAvailableSuggestion(object, patternInstance);
            }
        }
    }

    private void replaceAvailableSuggestions(PatternInstance patternInstance, PatternInstance patternInstanceFound) {
        PatternInstance mergedPatternInstance = new PatternInstance(patternInstanceFound, patternInstance);
        Set<PatternInstance> suggestedPatternInstances = availableSuggestions.get(object);
        suggestedPatternInstances.remove(patternInstanceFound);
        suggestedPatternInstances.add(mergedPatternInstance);
        availableSuggestions.put(object, suggestedPatternInstances);
    }

    private void replaceAcceptedSuggestions(PatternInstance patternInstance, PatternInstance patternInstanceFound) {
        patternInstanceFound.mergePatternParticipants(patternInstance);
        PatternInstance mergedPatternInstance = new PatternInstance(patternInstanceFound, patternInstance);
        Set<PatternInstance> suggestedPatternInstances = acceptedSuggestions.get(object);
        suggestedPatternInstances.remove(patternInstanceFound);
        suggestedPatternInstances.add(mergedPatternInstance);
        acceptedSuggestions.put(object, suggestedPatternInstances);
    }

    private void replaceSuggestionMapEntry(PatternInstance patternInstance, PatternInstance patternInstanceFound, Map<String, Set<PatternInstance>> suggestionsMap) {
        PatternInstance mergedPatternInstance = new PatternInstance(patternInstanceFound, patternInstance);
        Set<PatternInstance> suggestedPatternInstances = suggestionsMap.get(object);
        suggestedPatternInstances.remove(patternInstanceFound);
        suggestedPatternInstances.add(mergedPatternInstance);
        suggestionsMap.put(object, suggestedPatternInstances);
    }

    private PatternInstance getPatternInstanceInSuggestionMap(PatternInstance patternInstance, Map<String, Set<PatternInstance>> suggestionsMap) throws NullPointerException{
        Set<PatternInstance> suggestedPatternInstances = suggestionsMap.get(object);
        for (PatternInstance suggestedPatternInstance : suggestedPatternInstances) {
            if (patternInstance.areTheSamePatternInstance(suggestedPatternInstance)) {
                return suggestedPatternInstance;
            }
        }
        throw new NullPointerException();
    }

    void acceptAvailableSuggestion(PatternInstance patternInstance){
        for (Map.Entry<String, Set<String>> entry : patternInstance.getObjectRoles().entrySet()) {
            try {
                String object = entry.getKey();
                removeAvailableSuggestion(object, patternInstance);
                addAcceptedSuggestion(object, patternInstance);
            }
            catch (NullPointerException ignored){

            }
        }
    }

    private void moveAcceptedPatternInstanceToAvailable(String object, PatternInstance patternInstance){
        addAvailableSuggestion(object, patternInstance);
        removeAcceptedSuggestion(object, patternInstance);
    }

    private void addAcceptedSuggestion(String object, PatternInstance patternInstance) {
        Set<PatternInstance> acceptedPatternInstances = new HashSet<>();
        if(acceptedSuggestions.containsKey(object)){
            acceptedPatternInstances = acceptedSuggestions.get(object);
        }
        acceptedPatternInstances.add(patternInstance);
        acceptedSuggestions.put(object, acceptedPatternInstances);
    }

    private void removeAvailableSuggestion(String object, PatternInstance patternInstance) {
        Set<PatternInstance> availablePatternInstances = availableSuggestions.get(object);
        availablePatternInstances.remove(patternInstance);
        availableSuggestions.put(object, availablePatternInstances);
    }

    private void removeAcceptedSuggestion(String object, PatternInstance patternInstance) {
        Set<PatternInstance> acceptedPatternInstances = acceptedSuggestions.get(object);
        acceptedPatternInstances.remove(patternInstance);
        acceptedSuggestions.put(object, acceptedPatternInstances);
    }

    private PatternInstance getPatternInstanceInPersistentStorage(PatternInstance patternInstance) {
        ProjectPersistedState projectPersistedState = PluginState.getInstance().getProjectPersistedState();
        ConcurrentHashMap<String, PatternInstance> patternInstanceById = projectPersistedState.getPatternInstanceById();

        for (Map.Entry<String, PatternInstance> entry : patternInstanceById.entrySet()) {
            PatternInstance persistedPatternInstance = entry.getValue();
            if (patternInstance.areTheSamePatternInstance(persistedPatternInstance)) {
                return persistedPatternInstance;
            }
        }
        throw new NullPointerException();
    }

    private void addAvailableSuggestion(String object, PatternInstance joinedPatternInstance) {
        Set<PatternInstance> patternInstancesPlayedByObject = new HashSet<>();
        patternInstancesPlayedByObject.add(joinedPatternInstance);
        availableSuggestions.put(object, patternInstancesPlayedByObject);
    }
}
