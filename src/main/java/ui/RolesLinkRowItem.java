package ui;

import com.intellij.openapi.ui.ComboBox;

import javax.swing.*;

public class RolesLinkRowItem {

    private ComboBox role1, role2, linkType;
    private final JButton jButton;

    public RolesLinkRowItem(ComboBox role1, ComboBox linkType, ComboBox role2, JButton jButton){
        this.role1 = role1;
        this.linkType = linkType;
        this.role2 = role2;
        this.jButton = jButton;
    }

    public JButton getjButton() {
        return jButton;
    }

    public ComboBox getRole1() {
        return role1;
    }

    public ComboBox getRole2() {
        return role2;
    }

    public ComboBox getLinkType() {
        return linkType;
    }
}
