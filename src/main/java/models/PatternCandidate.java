package models;

import java.util.Set;

public class PatternCandidate {

    private Set<PatternParticipant> patternParticipants;
    private Set<String> roles;

    public PatternCandidate(Set<PatternParticipant> patternParticipants, Set<String> roles) {
        this.patternParticipants = patternParticipants;
        this.roles = roles;
    }

    public Set<PatternParticipant> getPatternParticipants() {
        return patternParticipants;
    }

    public Set<String> getRoles() {
        return roles;
    }
}
