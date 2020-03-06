package models;

import javax.swing.*;
import java.util.Objects;

public class CollaborationListItem {

    private final JTextField className;
    private final JTextField role;

    public CollaborationListItem(JTextField className, JTextField role){
        this.className = className;
        this.role = role;
    }

    public JTextField getClassName() {
        return className;
    }

    public JTextField getRole() {
        return role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CollaborationListItem that = (CollaborationListItem) o;
        return Objects.equals(className, that.className) &&
                Objects.equals(role, that.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, role);
    }
}
