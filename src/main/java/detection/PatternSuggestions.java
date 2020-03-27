package detection;

import models.PatternInstance;
import models.PatternParticipant;
import storage.PluginState;
import storage.ProjectDetails;
import storage.ProjectPersistedState;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PatternSuggestions {
    private ConcurrentHashMap<String, Set<PatternInstance>> acceptedSuggestions;
    private ConcurrentHashMap<String, Set<PatternInstance>> availableSuggestions;

    private PatternParticipant patternParticipant;

    public PatternSuggestions() {
        acceptedSuggestions = new ConcurrentHashMap<>();
        availableSuggestions = new ConcurrentHashMap<>();
    }

    public Map<String, Set<PatternInstance>> getAvailableSuggestions() {
        return availableSuggestions;
    }

    public void updateClassNameInSuggestions(String oldName, String newName) {
        if(acceptedSuggestions.containsKey(oldName)){
            updateClassNameInSuggestionMap(oldName, newName, acceptedSuggestions);
        }
        if(availableSuggestions.containsKey(oldName)){
            updateClassNameInSuggestionMap(oldName, newName, availableSuggestions);
        }
    }

    private void updateClassNameInSuggestionMap(String oldName, String newName, Map<String, Set<PatternInstance>> suggestionsMap) {
        Set<PatternInstance> patternInstances = suggestionsMap.get(oldName);
        for (PatternInstance patternInstance : patternInstances) {
            patternInstance.renameParticipantObject(oldName, newName);
        }
        suggestionsMap.put(newName, patternInstances);
        suggestionsMap.remove(oldName);
    }

    public void updateSuggestionsAfterManualDocumentation(PatternInstance patternInstance){
        Set<PatternParticipant> patternParticipants = patternInstance.getPatternParticipants();

        for (PatternParticipant patternParticipant : patternParticipants) {
            this.patternParticipant = patternParticipant;
            String object = patternParticipant.getObject();

            if (availableSuggestions.containsKey(object)) {
                try {
                    PatternInstance patternInstanceInSuggestionMap = getPatternInstanceInSuggestionMap(patternInstance, availableSuggestions);
                    Set<PatternParticipant> foundPatternParticipants = patternInstanceInSuggestionMap.getPatternParticipants();

                    if (foundPatternParticipants.contains(patternParticipant)) {
                        patternInstanceInSuggestionMap.updatePatternParticipantsContainers(patternInstance.getPatternParticipants());
                        moveAvailablePatternInstanceToAccepted(object, patternInstanceInSuggestionMap);
                    } else if (!patternInstance.equals(patternInstanceInSuggestionMap)) {
                        patternInstanceInSuggestionMap.updatePatternParticipantsContainers(patternInstance.getPatternParticipants());
                    }
                    continue;
                } catch (NullPointerException ignored) {
                }
            }

            if (acceptedSuggestions.containsKey(object)) {
                try {
                    updateAcceptedSuggestionsMap(patternInstance);
                } catch (NullPointerException ignored) {
                }
            }
        }
    }

    void acceptAvailableSuggestion(PatternInstance patternInstance) {
        Set<PatternParticipant> patternInstances = patternInstance.getPatternParticipants();
        for (PatternParticipant patternParticipant : patternInstances) {
            try {
                String object = patternParticipant.getObject();
                moveAvailablePatternInstanceToAccepted(object,patternInstance);
            } catch (NullPointerException ignored) {
            }
        }
    }

    private void moveAvailablePatternInstanceToAccepted(String object, PatternInstance patternInstance){
        removeSuggestionMapEntry(object, patternInstance, availableSuggestions);
        addSuggestionMapEntry(object, patternInstance, acceptedSuggestions);
    }

    private void removeSuggestionMapEntry(String object, PatternInstance patternInstance, Map<String, Set<PatternInstance>> suggestionMap) {
        Set<PatternInstance> patternInstances = suggestionMap.get(object);
        patternInstances.remove(patternInstance);
        suggestionMap.put(object, patternInstances);
    }

    private void addSuggestionMapEntry(String object, PatternInstance patternInstance, Map<String, Set<PatternInstance>> suggestionMap) {
        Set<PatternInstance> patternInstances = new HashSet<>();

        if (suggestionMap.containsKey(object)) {
            patternInstances = suggestionMap.get(object);
        }
        patternInstances.add(patternInstance);
        suggestionMap.put(object, patternInstances);
    }

    void updateSuggestions(Set<PatternInstance> patternInstances) {
        for (PatternInstance patternInstance : patternInstances) {
            Set<PatternParticipant> patternParticipants = patternInstance.getPatternParticipants();

            for (PatternParticipant patternParticipant : patternParticipants) {
                this.patternParticipant = patternParticipant;
                String object = patternParticipant.getObject();

                if (availableSuggestions.containsKey(object)) {
                    try {
                        updateAvailableSuggestionsMap(patternInstance);
                        continue;
                    } catch (NullPointerException ignored) {
                    }
                }

                if (acceptedSuggestions.containsKey(object)) {
                    try {
                        updateAcceptedSuggestionsMap(patternInstance);
                        continue;
                    } catch (NullPointerException ignored) {
                    }
                }

                try {
                    updateSuggestionMapsAccordingToPersistentStorage(patternInstance);
                    continue;
                } catch (NullPointerException ignored) {
                }

                addSuggestionMapEntry(object, patternInstance, availableSuggestions);
            }
        }
    }

    private void updateAvailableSuggestionsMap(PatternInstance patternInstance) throws NullPointerException {
        PatternInstance patternInstanceInSuggestionMap = getPatternInstanceInSuggestionMap(patternInstance, availableSuggestions);
        if (!patternInstance.equals(patternInstanceInSuggestionMap)) {
            patternInstanceInSuggestionMap.updatePatternParticipantsContainers(patternInstance.getPatternParticipants());
        }
    }

    private void updateAcceptedSuggestionsMap(PatternInstance patternInstance) throws NullPointerException {
        PatternInstance patternInstanceInSuggestionMap = getPatternInstanceInSuggestionMap(patternInstance, acceptedSuggestions);
        Set<PatternParticipant> patternParticipants = patternInstanceInSuggestionMap.getPatternParticipants();

        if (!patternParticipants.contains(patternParticipant)) {
            patternInstanceInSuggestionMap.updatePatternParticipantsContainers(patternInstance.getPatternParticipants());
            String object = patternParticipant.getObject();
            moveAcceptedPatternInstanceToAvailable(object, patternInstanceInSuggestionMap);
        } else if (!patternInstance.equals(patternInstanceInSuggestionMap)) {
            patternInstanceInSuggestionMap.updatePatternParticipantsContainers(patternInstance.getPatternParticipants());
        }
    }

    private void updateSuggestionMapsAccordingToPersistentStorage(PatternInstance patternInstance) throws NullPointerException {
        PatternInstance patternInstanceInPersistentStorage = getPatternInstanceInPersistentStorage(patternInstance);
        Set<PatternParticipant> patternParticipants = patternInstanceInPersistentStorage.getPatternParticipants();
        PatternInstance patternInstanceCopy = new PatternInstance(patternInstanceInPersistentStorage);
        patternInstanceCopy.updatePatternParticipantsContainers(patternInstance.getPatternParticipants());
        String object = patternParticipant.getObject();

        if (!patternParticipants.contains(patternParticipant)) {
            addSuggestionMapEntry(object, patternInstanceCopy, availableSuggestions);
        } else {
            addSuggestionMapEntry(object, patternInstanceCopy, acceptedSuggestions);
        }
    }

    private PatternInstance getPatternInstanceInSuggestionMap(PatternInstance patternInstance, Map<String, Set<PatternInstance>> suggestionsMap) throws NullPointerException {
        String object = patternParticipant.getObject();
        Set<PatternInstance> suggestedPatternInstances = suggestionsMap.get(object);

        for (PatternInstance suggestedPatternInstance : suggestedPatternInstances) {
            if (patternInstance.areTheSamePatternInstance(suggestedPatternInstance)) {
                return suggestedPatternInstance;
            }
        }
        throw new NullPointerException();
    }

    private void moveAcceptedPatternInstanceToAvailable(String object, PatternInstance patternInstance) {
        addSuggestionMapEntry(object, patternInstance, availableSuggestions);
        removeSuggestionMapEntry(object, patternInstance, acceptedSuggestions);
    }

    private PatternInstance getPatternInstanceInPersistentStorage(PatternInstance patternInstance) {
        ProjectDetails projectDetails = PluginState.getInstance().getProjectDetails();
        ProjectPersistedState projectPersistedState = projectDetails.getActiveProjectPersistedState();
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
