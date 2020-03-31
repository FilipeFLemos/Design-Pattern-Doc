package ui;

import com.intellij.openapi.ui.ComboBox;

import javax.swing.*;
import java.util.Objects;

public class CollaborationRowItem {

    private final JTextField className;
    private ComboBox role;
    private final JLabel jLabel;
    private final JButton jButton;

    public CollaborationRowItem(JTextField className, ComboBox role, JLabel jLabel, JButton jButton) {
        this.className = className;
        this.role = role;
        this.jLabel = jLabel;
        this.jButton = jButton;
    }

    public JTextField getClassName() {
        return className;
    }

    public ComboBox getRole() {
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
        CollaborationRowItem listItem = (CollaborationRowItem) o;
        return Objects.equals(className, listItem.className) &&
                Objects.equals(role, listItem.role) &&
                Objects.equals(jLabel, listItem.jLabel) &&
                Objects.equals(jButton, listItem.jButton);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, role, jLabel, jButton);
    }

    public void setRole(ComboBox role) {
        this.role = role;
    }
}
