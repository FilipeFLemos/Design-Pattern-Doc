package models;

import java.io.Serializable;
import java.util.Objects;

public class RolesLink implements Serializable {

    private static final long serialVersionUID = 19L;

    private String role1;
    private String role2;
    private String linkType;

    public RolesLink(){

    }

    public RolesLink(String role1, String role2, String linkType){
        this.role1 = role1;
        this.role2 = role2;
        this.linkType = linkType;
    }

    public String getRole1() {
        return role1;
    }

    public void setRole1(String role1) {
        this.role1 = role1;
    }

    public String getRole2() {
        return role2;
    }

    public void setRole2(String role2) {
        this.role2 = role2;
    }

    public String getLinkType() {
        return linkType;
    }

    public void setLinkType(String linkType) {
        this.linkType = linkType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RolesLink rolesLink = (RolesLink) o;
        return Objects.equals(role1, rolesLink.role1) &&
                Objects.equals(role2, rolesLink.role2) &&
                Objects.equals(linkType, rolesLink.linkType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(role1, role2, linkType);
    }
}
