package models;

import javax.swing.*;
import java.util.Objects;

public class CollaborationListItem {

    private final JTextField className;
    private final JTextField role;
    private final JLabel jLabel;
    private final JButton jButton;

    public CollaborationListItem(JTextField className, JTextField role, JLabel jLabel, JButton jButton){
        this.className = className;
        this.role = role;
        this.jLabel = jLabel;
        this.jButton = jButton;
    }

    public JTextField getClassName() {
        return className;
    }

    public JTextField getRole() {
        return role;
    }

    public JLabel getjLabel() {
        return jLabel;
    }

    public JButton getjButton() {
        return jButton;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CollaborationListItem listItem = (CollaborationListItem) o;
        return Objects.equals(className, listItem.className) &&
                Objects.equals(role, listItem.role) &&
                Objects.equals(jLabel, listItem.jLabel) &&
                Objects.equals(jButton, listItem.jButton);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, role, jLabel, jButton);
    }
}
