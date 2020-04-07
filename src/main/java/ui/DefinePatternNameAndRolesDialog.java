package ui;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import models.DesignPattern;
import org.jetbrains.annotations.Nullable;
import storage.PluginState;
import storage.ProjectsPersistedState;
import utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class DefinePatternNameAndRolesDialog extends DialogWrapper {

    private JPanel panel;
    private JPanel rolesPanel;
    private JBScrollPane rolesScrollPane;
    private JTextField patternName;
    private JButton addRoleBtn;
    private ArrayList<RolesRowItem> rolesRowList;
    private int gridHeight;
    private final int MIN_NUM_ROWS = 3;
    public final int NEXT_STEP_DIALOG = 3;

    private ProjectsPersistedState projectsPersistedState;
    private String name;
    private Set<String> roles;

    public DefinePatternNameAndRolesDialog(boolean canBeParent) {
        super(canBeParent);

        panel = new JPanel(new GridBagLayout());

        patternName = new JTextField();
        gridHeight = 0;

        rolesPanel = new JPanel(new GridBagLayout());
        rolesRowList = new ArrayList<>();

        rolesScrollPane = new JBScrollPane(rolesPanel);
        rolesScrollPane.setPreferredSize(new Dimension(350, 100));
        rolesScrollPane.createVerticalScrollBar();

        addRoleBtn = new JButton("Add Role");
        addRoleBtn.addActionListener(e -> {
            changeDeleteBtnVisibilityWhenOnlyOneRow(true);
            addRolesRowToPanel();
            updateRolesPanel();
        });

        setResizable(false);

        try {
            projectsPersistedState = PluginState.getInstance().getState();
        } catch (NullPointerException ignored) {

        }

        setTitle("Define Design Pattern - Step 1");
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        addRowElement(Utils.getFieldLabel("Pattern Name"));
        addRowElement(patternName);
        addRolesHeaderToPanel();
        addRowElement(rolesScrollPane);
        addRoleListToPanel();
        changeDeleteBtnVisibilityWhenOnlyOneRow(false);

        return panel;
    }

    private void addRowElement(JComponent jComponent) {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = gridHeight;
        panel.add(jComponent, c);

        gridHeight++;
    }

    private void addRolesHeaderToPanel() {
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = gridHeight;
        c.insets = JBUI.insets(5, 0, 5, 0);
        panel.add(Utils.getFieldLabel("Roles"), c);

        c.anchor = GridBagConstraints.EAST;
        c.gridwidth = 1;
        c.gridx = 1;
        panel.add(addRoleBtn, c);

        gridHeight++;
    }

    private void addRoleListToPanel() {
        for (int i = 0; i < MIN_NUM_ROWS; i++) {
            addRolesRowToPanel();
        }
    }

    private void addRolesRowToPanel() {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 1;
        c.weightx = 1.0;
        c.gridx = 0;
        c.gridy = gridHeight;
        JTextField role = new JTextField();
        rolesPanel.add(role, c);


        c.weightx = 0.0;
        c.gridx = 1;
        JButton deleteRowBtn = new JButton("X");
        rolesPanel.add(deleteRowBtn, c);

        RolesRowItem rolesRowItem = new RolesRowItem(role, deleteRowBtn);
        rolesRowList.add(rolesRowItem);
        gridHeight++;

        deleteRowBtn.addActionListener(e -> {
            rolesPanel.remove(role);
            rolesPanel.remove(deleteRowBtn);
            rolesRowList.remove(rolesRowItem);
            changeDeleteBtnVisibilityWhenOnlyOneRow(false);
            updateRolesPanel();
        });
    }

    private void updateRolesPanel() {
        rolesPanel.revalidate();
        rolesPanel.repaint();
    }

    protected void changeDeleteBtnVisibilityWhenOnlyOneRow(boolean b) {
        if (rolesRowList.size() == 1) {
            RolesRowItem rolesRowItem = rolesRowList.get(0);
            JButton deleteBtn = rolesRowItem.getjButton();
            deleteBtn.setVisible(b);
        }
    }

    @Override
    protected ValidationInfo doValidate() {
        String name = patternName.getText();
        if (name.equals("")) {
            return new ValidationInfo("This field is mandatory!", patternName);
        }

        Set<String> roles = new HashSet<>();

        for (RolesRowItem listItem : rolesRowList) {
            String role = listItem.getRole().getText();
            if (role.equals("")) {
                return new ValidationInfo("This field is mandatory! If you do not need the row, consider removing it.", listItem.getRole());
            }

            if(roles.contains(role)){
                return new ValidationInfo("This role has already been specified above!", listItem.getRole());
            }
            roles.add(role);
        }

        Set<DesignPattern> supportedDesignPatterns = projectsPersistedState.getSupportedDesignPatterns();
        for (DesignPattern designPattern : supportedDesignPatterns) {
            String supportedPatternName = designPattern.getName();
            if (supportedPatternName.equals(name)) {
                return new ValidationInfo("This design pattern is already supported.");
            }
        }

        return null;
    }

    @Override
    protected void doOKAction() {
        setDesignPatternNameAndRolesFromUserInput();
        close(NEXT_STEP_DIALOG);
    }

    private void setDesignPatternNameAndRolesFromUserInput() {
        name = patternName.getText();
        roles = new HashSet<>();

        for (RolesRowItem listItem : rolesRowList) {
            String role = listItem.getRole().getText();
            roles.add(role);
        }
    }

    public String getName() {
        return name;
    }

    public Set<String> getRoles() {
        return roles;
    }
}
