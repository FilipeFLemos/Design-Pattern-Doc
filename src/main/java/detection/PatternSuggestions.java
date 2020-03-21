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

    private PatternParticipant patternParticipant;

    public PatternSuggestions() {
        acceptedSuggestions = new HashMap<>();
        availableSuggestions = new HashMap<>();
    }

    public Map<String, Set<PatternInstance>> getAvailableSuggestions() {
        return availableSuggestions;
    }

    void acceptAvailableSuggestion(PatternInstance patternInstance) {
        for (Map.Entry<String, Set<String>> entry : patternInstance.getObjectRoles().entrySet()) {
            try {
                String object = entry.getKey();
                removeSuggestionMapEntry(object, patternInstance, availableSuggestions);
                addSuggestionMapEntry(object, patternInstance, acceptedSuggestions);
            } catch (NullPointerException ignored) {
            }
        }
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
