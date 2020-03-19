package models;

import java.util.Objects;

public class PatternParticipant {

    private String object;
    private String role;

    public PatternParticipant(String object, String role){
        this.object = object;
        this.role = role;
    }

    public String getObject() {
        return object;
    }

    public String getRole() {
        return role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PatternParticipant patternParticipant = (PatternParticipant) o;
        return Objects.equals(object, patternParticipant.object) &&
                Objects.equals(role, patternParticipant.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(object, role);
    }
}
