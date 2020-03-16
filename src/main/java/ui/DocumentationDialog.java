package ui;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import models.CollaborationListItem;
import models.PatternInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import storage.PluginState;
import storage.ProjectPersistedState;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DocumentationDialog extends DialogWrapper {

    protected final int DEFAULT_NUM_ROWS = 3;
    protected JPanel panel;
    protected JTextField patternName;
    protected JTextArea patternIntent;
    protected JButton addCollaborationRowBtn;
    protected ArrayList<CollaborationListItem> collaborationList;
    protected int gridHeight = 0;
    protected int numCollaborationRows;

    protected ProjectPersistedState projectPersistedState;
    protected ConcurrentHashMap<String, PatternInstance> patternInstanceById;
    private Map<String, Set<String>> roleObjects;
    private Map<String, Set<String>> objectRoles;

    public DocumentationDialog(boolean canBeParent) {
        super(canBeParent);

        panel = new JPanel(new GridBagLayout());
        patternName = new JTextField();
        patternIntent = new JTextArea();
        patternIntent.setLineWrap(true);
        addCollaborationRowBtn = new JButton("Add Row");
        collaborationList = new ArrayList<>();

        try {
            setProjectState();
            setPatternInstanceById();
        } catch (Exception ignored) {

        }

        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return null;
    }

    protected void addDocumentationDialogInvariableBody() {
        addElementToPanel(getLabel("Pattern Name"));
        addElementToPanel(patternName);
        addElementToPanel(getLabel("Intent"));
        addElementToPanel(patternIntent);
        addCollaborationHeaderToPanel();
        addCollaborationListToPanel();

        addCollaborationRowBtn.addActionListener(e -> {
            addCollaborationRowToPanel();
            updatePanel();
        });
    }

    protected JBLabel getLabel(String text) {
        JBLabel jLabel = new JBLabel(text);
        jLabel.setComponentStyle(UIUtil.ComponentStyle.SMALL);
        jLabel.setFontColor(UIUtil.FontColor.BRIGHTER);
        jLabel.setBorder(JBUI.Borders.empty(0, 5, 2, 0));
        return jLabel;
    }

    protected void addElementToPanel(JComponent jComponent) {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 4;
        c.gridx = 0;
        c.gridy = gridHeight;
        c.insets = JBUI.insetsBottom(5);
        panel.add(jComponent, c);

        gridHeight++;
    }

    protected void addCollaborationHeaderToPanel() {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 3;
        c.gridx = 0;
        c.gridy = gridHeight;
        c.insets = JBUI.insets(5, 0, 5, 0);
        panel.add(getLabel("Collaborations (Class -> Role)"), c);

        c.gridx = 3;
        panel.add(addCollaborationRowBtn, c);

        gridHeight++;
    }

    protected void addCollaborationListToPanel() {
        for (int i = 0; i < numCollaborationRows; i++) {
            addCollaborationRowToPanel();
        }
    }

    protected void addCollaborationRowToPanel() {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        JTextField object = new JTextField();
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = gridHeight;
        panel.add(object, c);

        c.weightx = 0.0;
        c.gridx = 1;
        JLabel arrow = getLabel("->");
        panel.add(arrow, c);

        JTextField role = new JTextField();
        c.weightx = 1.0;
        c.gridx = 2;
        panel.add(role, c);

        JButton deleteRowBtn = new JButton("X");
        c.weightx = 0.0;
        c.gridx = 3;
        panel.add(deleteRowBtn, c);

        CollaborationListItem listItem = new CollaborationListItem(object, role, arrow, deleteRowBtn);
        collaborationList.add(listItem);
        gridHeight++;

        deleteRowBtn.addActionListener(e -> {
            removeCollaborationRowObjects(listItem);
            updatePanel();
            gridHeight--;
            collaborationList.remove(listItem);
        });
    }

    protected void updatePanel() {
        panel.revalidate();
        panel.repaint();
    }

    protected void removeCollaborationRowObjects(CollaborationListItem listItem) {
        JTextField className = listItem.getClassName();
        JTextField role = listItem.getRole();
        JLabel jLabel = listItem.getjLabel();
        JButton jButton = listItem.getjButton();

        panel.remove(className);
        panel.remove(role);
        panel.remove(jLabel);
        panel.remove(jButton);
    }

    protected void removeAllCollaborationRoles() {
        for (CollaborationListItem listItem : collaborationList) {
            removeCollaborationRowObjects(listItem);
        }

        gridHeight -= collaborationList.size();
        collaborationList = new ArrayList<>();
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        String name = patternName.getText();
        if (name.equals("")) {
            return new ValidationInfo("This field is mandatory!", patternName);
        }

        if (collaborationList.isEmpty()) {
            return new ValidationInfo("There must be at least one collaboration row!", addCollaborationRowBtn);
        }

        for (CollaborationListItem listItem : collaborationList) {
            String className = listItem.getClassName().getText();
            String role = listItem.getRole().getText();

            if (className.equals("")) {
                return new ValidationInfo("This field is mandatory! If you do not need the row, consider removing it.", listItem.getClassName());
            } else if (role.equals("")) {
                return new ValidationInfo("This field is mandatory! If you do not need the row, consider removing it.", listItem.getRole());
            }
        }

        return null;
    }

    @NotNull
    protected PatternInstance generatePatternInstanceFromUserInput() {
        String name = patternName.getText();
        String intent = patternIntent.getText();

        roleObjects = new HashMap<>();
        objectRoles = new HashMap<>();

        for (CollaborationListItem listItem : collaborationList) {
            String className = listItem.getClassName().getText();
            String role = listItem.getRole().getText();

            updateObjectRoles(className, role);
            updateRoleObjects(className, role);
        }

        return new PatternInstance(name, intent, roleObjects, objectRoles);
    }

    private void updateRoleObjects(String className, String role) {
        updateMapStringSet(className, role, roleObjects);
    }

    private void updateObjectRoles(String className, String role) {
        updateMapStringSet(className, role, objectRoles);
    }

    private void updateMapStringSet(String mapKey, String newSetElement, Map<String, Set<String>> map) {
        Set<String> mapValue = new HashSet<>();
        if (map.containsKey(mapKey)) {
            mapValue = map.get(mapKey);
        }
        mapValue.add(newSetElement);
        map.put(mapKey, mapValue);
    }

    protected void setProjectState() throws NullPointerException {
        this.projectPersistedState = ((PluginState) PluginState.getInstance()).getProjectPersistedState();
    }

    protected void setPatternInstanceById() throws NullPointerException {
        this.patternInstanceById = projectPersistedState.getPatternInstanceById();
        if (this.patternInstanceById == null)
            throw new NullPointerException();
    }

    protected void setNumCollaborationRows(int numCollaborationRows) {
        this.numCollaborationRows = numCollaborationRows;
    }
}
