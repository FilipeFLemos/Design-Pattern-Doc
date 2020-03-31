package ui;

import javax.swing.*;
import java.util.Objects;

public class RolesRowItem {

    private final JTextField role;
    private final JButton jButton;

    public RolesRowItem(JTextField role, JButton jButton) {
        this.role = role;
        this.jButton = jButton;
    }

    public JTextField getRole() {
        return role;
    }

    public JButton getjButton() {
        return jButton;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RolesRowItem that = (RolesRowItem) o;
        return Objects.equals(role, that.role) &&
                Objects.equals(jButton, that.jButton);
    }

    @Override
    public int hashCode() {
        return Objects.hash(role, jButton);
    }
}
