package ui;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import models.DesignPattern;
import models.RolesLink;
import org.jetbrains.annotations.Nullable;
import storage.PluginState;
import storage.ProjectsPersistedState;
import utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class DefinePatternRolesLinksDialog extends DialogWrapper {

    private JPanel panel;
    private JPanel linksPanel;
    private JBScrollPane linksScrollPane;
    private JButton addRolesLinkRowBtn;
    private ArrayList<RolesLinkRowItem> rolesLinkRowList;
    private int gridHeight;
    private final int MIN_NUM_ROWS = 2;

    private ProjectsPersistedState projectsPersistedState;
    private String patternName;
    private Set<String> roles;

    public DefinePatternRolesLinksDialog(boolean canBeParent, String patternName, Set<String> roles) {
        super(canBeParent);

        this.patternName = patternName;
        this.roles = roles;

        panel = new JPanel(new GridBagLayout());
        gridHeight = 0;
        linksPanel = new JPanel(new GridBagLayout());
        rolesLinkRowList = new ArrayList<>();

        linksScrollPane = new JBScrollPane(linksPanel);
        linksScrollPane.setPreferredSize(new Dimension(350, 100));
        linksScrollPane.createVerticalScrollBar();

        addRolesLinkRowBtn = new JButton("Add Roles Link");
        addRolesLinkRowBtn.addActionListener(e -> {
            changeDeleteBtnVisibilityWhenOnlyOneRow(true);
            addRolesLinkRowToPanel();
            updateRolesPanel();
        });

        setResizable(false);

        try {
            projectsPersistedState = PluginState.getInstance().getState();
        } catch (NullPointerException ignored) {

        }

        setTitle("Define Design Pattern - Step 2");
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        addRolesHeaderToPanel();
        addRowElement(linksScrollPane);
        addRolesLinkListToPanel();
        changeDeleteBtnVisibilityWhenOnlyOneRow(false);

        return panel;
    }

    private void addRowElement(JComponent jComponent) {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 3;
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
        panel.add(Utils.getFieldLabel("Links between Roles:"), c);

        c.anchor = GridBagConstraints.EAST;
        c.gridwidth = 1;
        c.gridx = 2;
        panel.add(addRolesLinkRowBtn, c);

        gridHeight++;
    }

    private void addRolesLinkListToPanel() {
        for (int i = 0; i < MIN_NUM_ROWS; i++) {
            addRolesLinkRowToPanel();
        }
    }

    private void addRolesLinkRowToPanel() {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 1;
        c.weightx = 1.0;
        c.gridx = 0;
        c.gridy = gridHeight;
        ComboBox role1 = getRolesComboBox();
        linksPanel.add(role1, c);

        c.gridwidth = 1;
        c.weightx = 1.0;
        c.gridx = 1;
        c.gridy = gridHeight;
        ComboBox linkType = getLinkTypeComboBox();
        linksPanel.add(linkType, c);

        c.gridwidth = 1;
        c.weightx = 1.0;
        c.gridx = 2;
        c.gridy = gridHeight;
        ComboBox role2 = getRolesComboBox();
        linksPanel.add(role2, c);

        c.weightx = 0.0;
        c.gridx = 3;
        JButton deleteRowBtn = new JButton("X");
        linksPanel.add(deleteRowBtn, c);

        RolesLinkRowItem rolesLinkRowItem = new RolesLinkRowItem(role1, linkType, role2, deleteRowBtn);
        rolesLinkRowList.add(rolesLinkRowItem);
        gridHeight++;

        deleteRowBtn.addActionListener(e -> {
            linksPanel.remove(role1);
            linksPanel.remove(linkType);
            linksPanel.remove(role2);
            linksPanel.remove(deleteRowBtn);
            rolesLinkRowList.remove(rolesLinkRowItem);
            changeDeleteBtnVisibilityWhenOnlyOneRow(false);
            updateRolesPanel();
        });
    }

    private ComboBox getLinkTypeComboBox() {
        String[] linkTypes = new String[]{"inherits", "uses", "has", "creates", "calls", "references"};
        return new ComboBox(linkTypes);
    }

    private ComboBox getRolesComboBox() {
        List<String> rolesList = new ArrayList<>(roles);
        String[] roles = new String[rolesList.size()];
        roles = rolesList.toArray(roles);
        return new ComboBox(roles);
    }

    private void updateRolesPanel() {
        linksPanel.revalidate();
        linksPanel.repaint();
    }

    protected void changeDeleteBtnVisibilityWhenOnlyOneRow(boolean b) {
        if (rolesLinkRowList.size() == 1) {
            RolesLinkRowItem rolesLinkRowItem = rolesLinkRowList.get(0);
            JButton deleteBtn = rolesLinkRowItem.getjButton();
            deleteBtn.setVisible(b);
        }
    }

    @Override
    protected ValidationInfo doValidate() {

        Set<RolesLink> rolesLinkRowItemSet = new HashSet<>();

        for (RolesLinkRowItem listItem : rolesLinkRowList) {
            String role1 = (String) listItem.getRole1().getSelectedItem();
            String role2 = (String) listItem.getRole2().getSelectedItem();
            String linkType = (String) listItem.getLinkType().getSelectedItem();
            if (role1 == null || role1.equals("")) {
                return new ValidationInfo("This field is mandatory! If you do not need the row, consider removing it.", listItem.getRole1());
            }
            else if(role2 == null || role2.equals("")){
                return new ValidationInfo("This field is mandatory! If you do not need the row, consider removing it.", listItem.getRole2());
            }

            RolesLink rolesLink = new RolesLink(role1, role2, linkType);
            if(rolesLinkRowItemSet.contains(rolesLink)){
                return new ValidationInfo("This row has already been specified above!", listItem.getRole1());
            }
            rolesLinkRowItemSet.add(rolesLink);
        }

        return null;
    }

    @Override
    protected void doOKAction() {
        DesignPattern designPattern = generateDesignPatternFromUserInput();
        projectsPersistedState.storeDesignPattern(designPattern);
        close(OK_EXIT_CODE);
    }

    private DesignPattern generateDesignPatternFromUserInput() {

        List<RolesLink> rolesLinks = new ArrayList<>();

        for (RolesLinkRowItem listItem : rolesLinkRowList) {
            String role1 = (String) listItem.getRole1().getSelectedItem();
            String role2 = (String) listItem.getRole2().getSelectedItem();
            String linkType = (String) listItem.getLinkType().getSelectedItem();
            rolesLinks.add(new RolesLink(role1, role2, linkType));
        }

        return new DesignPattern(patternName, roles, rolesLinks);
    }
}
