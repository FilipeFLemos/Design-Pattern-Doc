package models;

import java.util.Objects;

public class Relation {

    private String object1;
    private String object2;

    public Relation(String object1, String object2){
        this.object1 = object1;
        this.object2 = object2;
    }

    public String getObject1() {
        return object1;
    }

    public void setObject1(String object1) {
        this.object1 = object1;
    }

    public String getObject2() {
        return object2;
    }

    public void setObject2(String object2) {
        this.object2 = object2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Relation relation = (Relation) o;
        return Objects.equals(object1, relation.object1) &&
                Objects.equals(object2, relation.object2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(object1, object2);
    }
}
