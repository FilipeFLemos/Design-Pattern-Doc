package models;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.*;

public class DesignPattern implements Serializable, Comparable<DesignPattern> {

    private static final long serialVersionUID = 1L;

    private String name;
    private Set<String> roles;
    private List<ClassLink> rolesLinks;

    public DesignPattern() {

    }

    public DesignPattern(String name, Set<String> roles, List<ClassLink> rolesLinks) {
        this.name = name;
        this.roles = roles;
        this.rolesLinks = rolesLinks;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public List<ClassLink> getRolesLinks() {
        return rolesLinks;
    }

    public void setRolesLinks(List<ClassLink> rolesLinks) {
        this.rolesLinks = rolesLinks;
    }

    @Override
    public int compareTo(@NotNull DesignPattern o) {
        return this.name.compareTo(o.getName());
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
    public int hashCode() {
        return Objects.hash(name, roles);
    }
}
