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

    void acceptAvailableSuggestion(PatternInstance patternInstance){
        for (Map.Entry<String, Set<String>> entry : patternInstance.getObjectRoles().entrySet()) {
            try {
                String object = entry.getKey();
                removeSuggestionMapEntry(object, patternInstance, availableSuggestions);
                addSuggestionMapEntry(object, patternInstance, acceptedSuggestions);
            }
            catch (NullPointerException ignored){

            }
        }
    }

    private void removeSuggestionMapEntry(String object, PatternInstance patternInstance, Map<String, Set<PatternInstance>> suggestionMap){
        Set<PatternInstance> patternInstances = suggestionMap.get(object);
        patternInstances.remove(patternInstance);
        suggestionMap.put(object, patternInstances);
    }

    private void addSuggestionMapEntry(String object, PatternInstance patternInstance, Map<String, Set<PatternInstance>> suggestionMap){
        Set<PatternInstance> patternInstances = new HashSet<>();
        if(suggestionMap.containsKey(object)){
            patternInstances = suggestionMap.get(object);
        }
        patternInstances.add(patternInstance);
        suggestionMap.put(object, patternInstances);
    }

    void updateSuggestions(Set<PatternInstance> patternInstances) {
        for (PatternInstance patternInstance : patternInstances) {
            Set<PatternParticipant> patternParticipants = patternInstance.getPatternParticipants();
            for (PatternParticipant patternParticipant : patternParticipants) {
                object = patternParticipant.getObject();
                role = patternParticipant.getRole();

                if (availableSuggestions.containsKey(object)) {
                    try{
                        PatternInstance patternInstanceFound = getPatternInstanceInSuggestionMap(patternInstance, availableSuggestions);
                        if (!patternInstance.equals(patternInstanceFound)) {
                            patternInstanceFound.updatePatternParticipantsContainers(patternInstance.getPatternParticipants());
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
                            patternInstanceFound.updatePatternParticipantsContainers(patternInstance.getPatternParticipants());
                            moveAcceptedPatternInstanceToAvailable(object, patternInstanceFound);
                        }
                        else if(!patternInstance.equals(patternInstanceFound)){
                            patternInstanceFound.updatePatternParticipantsContainers(patternInstance.getPatternParticipants());
                        }
                        continue;
                    }catch (NullPointerException ignored){

                    }
                }

                try{
                    PatternInstance patternInstanceFound = getPatternInstanceInPersistentStorage(patternInstance);
                    Set<String> roles = patternInstanceFound.getObjectRoles().get(object);
                    PatternInstance copyPatternInstance = new PatternInstance(patternInstanceFound);
                    copyPatternInstance.updatePatternParticipantsContainers(patternInstance.getPatternParticipants());
                    if (roles == null || !roles.contains(role)) {
                        addSuggestionMapEntry(object, copyPatternInstance, availableSuggestions);
                    }
                    else if(roles.contains(role)){
                        addSuggestionMapEntry(object, copyPatternInstance, acceptedSuggestions);
                    }
                    continue;
                }catch (NullPointerException ignored){
                }

                addSuggestionMapEntry(object, patternInstance, availableSuggestions);
            }
        }
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

    private void moveAcceptedPatternInstanceToAvailable(String object, PatternInstance patternInstance){
        addSuggestionMapEntry(object, patternInstance, availableSuggestions);
        removeSuggestionMapEntry(object, patternInstance, acceptedSuggestions);
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
}
