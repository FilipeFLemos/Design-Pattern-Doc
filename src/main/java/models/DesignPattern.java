package models;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

public class DesignPattern implements Serializable, Comparable<DesignPattern> {

    private static final long serialVersionUID = 1L;

    private String name;
    private Set<String> roles;

    public DesignPattern(){

    }

    public DesignPattern(String name, Set<String> roles){
        this.name = name;
        this.roles = roles;
    }

    public String getName() {
        return name;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DesignPattern that = (DesignPattern) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(roles, that.roles);
    }

    @Override
    public int compareTo(@NotNull DesignPattern o) {
        return this.name.compareTo(o.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, roles);
    }
}
