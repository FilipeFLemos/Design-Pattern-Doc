package models;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

public class PatternParticipant implements Serializable, Comparable<PatternParticipant> {

    private static final long serialVersionUID = 1L;

    private String object;
    private String role;

    public PatternParticipant() {

    }

    public PatternParticipant(String object, String role) {
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

    @Override
    public int compareTo(@NotNull PatternParticipant o) {
        int result = this.object.compareTo(o.getObject());
        return result == 0 ? this.role.compareTo(o.role) : result;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
