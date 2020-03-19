package models;

import java.util.Set;

public class PatternCandidate {

    private Set<PatternParticipant> patternParticipants;

    public PatternCandidate(Set<PatternParticipant> patternParticipants) {
        this.patternParticipants = patternParticipants;
    }

    public Set<PatternParticipant> getPatternParticipants() {
        return patternParticipants;
    }
}
