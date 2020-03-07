package ui;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.util.ui.JBUI;
import models.CollaborationListItem;
import models.PatternInstance;
import models.Relation;
import org.jetbrains.annotations.Nullable;
import storage.PersistentState;
import storage.PluginState;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EditDocumentationDialog extends DocumentationDialog{

    private ComboBox jComboBox;
    private JButton deletePatternInstance;

    public EditDocumentationDialog(boolean canBeParent) {
        super(canBeParent);
        setTitle("Edit Pattern Instance Documentation");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        panel.setPreferredSize(new Dimension(500,200));

        PersistentState persistentState = (PersistentState) PluginState.getInstance().getState();
        ConcurrentHashMap<String, PatternInstance> patternInstanceById = persistentState.getPatternInstanceById();

        setJComboBox(patternInstanceById);
        setJComboListener(patternInstanceById);

        String id = (String) jComboBox.getSelectedItem();
        numOfCollaborationRows = patternInstanceById.get(id).getCollaborationRows().size();

        addElementToPanel(getLabel("Stored Pattern Instances"));
        addPatternInstancesHeaderToPanel();
        addDocumentationInvariableBody();

        fillFields();

        return panel;
    }

    private void addPatternInstancesHeaderToPanel() {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 3;
        c.gridx = 0;
        c.gridy = gridHeight;
        c.insets = JBUI.insetsBottom(5);
        panel.add(jComboBox, c);

        deletePatternInstance = new JButton("DELETE");
        setDeletePatternInstanceDocListener();

        c.gridx = 3;
        c.insets = JBUI.insetsLeft(5);
        panel.add(deletePatternInstance,c);

        gridHeight++;
    }

    private void setDeletePatternInstanceDocListener() {
        deletePatternInstance.addActionListener(e -> {
            PersistentState persistentState = (PersistentState) PluginState.getInstance().getState();

            String id = (String) jComboBox.getSelectedItem();
            persistentState.deletePatternInstance(id);

            close(NEXT_USER_EXIT_CODE);
        });
    }

    private void setJComboListener(ConcurrentHashMap<String, PatternInstance> patternInstanceById) {
        jComboBox.addActionListener(e->{
            String id = (String) jComboBox.getSelectedItem();
            int numRows = patternInstanceById.get(id).getCollaborationRows().size();
            removeCollaborationRoles();
            addCollaborationListToPanel(numRows);
            fillFields();
            panel.revalidate();
            panel.repaint();
        });
    }

    private void setJComboBox(ConcurrentHashMap<String, PatternInstance> patternInstanceById) {
        String[] patternInstancesIds = new String[patternInstanceById.size()];
        int index = 0;
        for (Map.Entry<String, PatternInstance> entry : patternInstanceById.entrySet()) {
            patternInstancesIds[index] = entry.getKey();
            index++;
        }
        jComboBox = new ComboBox(patternInstancesIds);
    }

    private void fillFields() {
        PersistentState persistentState = (PersistentState) PluginState.getInstance().getState();

        String id = (String) jComboBox.getSelectedItem();
        PatternInstance patternInstance = persistentState.getPatternInstance(id);

        patternName.setText(patternInstance.getPatternName());
        patternIntent.setText(patternInstance.getIntent());

        ArrayList<Relation> collaborationRows = patternInstance.getCollaborationRows();
        int index = 0;
        for(CollaborationListItem listItem : collaborationList){
            JTextField classNameField = listItem.getClassName();
            JTextField roleField = listItem.getRole();

            Relation relation = collaborationRows.get(index);
            String className = relation.getObject1();
            String role = relation.getObject2();

            classNameField.setText(className);
            roleField.setText(role);

            index++;
        }
    }

    @Override
    protected void doOKAction() {
        PatternInstance patternInstance = generatePatternInstanceFromUserInput();

        PersistentState persistentState = (PersistentState) PluginState.getInstance().getState();

        String id = (String) jComboBox.getSelectedItem();
        persistentState.updatePatternInstance(id, patternInstance);

        close(OK_EXIT_CODE);
    }
}
