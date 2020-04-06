package models;

import java.io.Serializable;

public class ClassLink implements Serializable {

    private static final long serialVersionUID = 19L;

    private String role1;
    private String role2;
    private String classLinkType;

    public ClassLink(){

    }

    public ClassLink(String role1, String role2, String classLinkType){
        this.role1 = role1;
        this.role2 = role2;
        this.classLinkType = classLinkType;
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

    public String getClassLinkType() {
        return classLinkType;
    }

    public void setClassLinkType(String classLinkType) {
        this.classLinkType = classLinkType;
    }
}
