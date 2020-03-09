package ui;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.util.ui.JBUI;
import models.CollaborationListItem;
import models.PatternInstance;
import models.Relation;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Map;

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
        try{
            panel.setPreferredSize(new Dimension(500,200));

            setJComboBox();
            setJComboListener();
            setSelectedPatternInstanceNumCollaborationRows();

            addElementToPanel(getLabel("Stored Pattern Instances"));
            addPatternInstancesHeaderToPanel();
            addDocumentationDialogInvariableBody();

            fillFields();
        }catch(Exception ignored){

        }

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
        deletePatternInstance.addActionListener(e ->
        {
            String id = getSelectedPatternInstanceId();
            projectState.deletePatternInstance(id);
            close(NEXT_USER_EXIT_CODE);
        });
    }

    private void setJComboListener() {
        jComboBox.addActionListener(e->
        {
            setSelectedPatternInstanceNumCollaborationRows();

            removeAllCollaborationRoles();
            addCollaborationListToPanel();
            fillFields();
            updatePanel();
        });
    }

    private void setSelectedPatternInstanceNumCollaborationRows() {
        String id = getSelectedPatternInstanceId();
        int numRows = patternInstanceById.get(id).getCollaborationRows().size();
        setNumCollaborationRows(numRows);
    }

    private void setJComboBox() {
        String[] patternInstancesIds = new String[patternInstanceById.size()];
        int index = 0;
        for (Map.Entry<String, PatternInstance> entry : patternInstanceById.entrySet()) {
            patternInstancesIds[index] = entry.getKey();
            index++;
        }
        jComboBox = new ComboBox(patternInstancesIds);
    }

    private void fillFields(){
        String id = getSelectedPatternInstanceId();
        PatternInstance patternInstance = projectState.getPatternInstance(id);

        patternName.setText(patternInstance.getPatternName());
        patternIntent.setText(patternInstance.getIntent());
        fillCollaborationRows(patternInstance);
    }

    private void fillCollaborationRows(PatternInstance patternInstance) {
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
        String id = getSelectedPatternInstanceId();

        projectState.updatePatternInstance(id, patternInstance);
        close(OK_EXIT_CODE);
    }

    private String getSelectedPatternInstanceId() throws NullPointerException {
        String id = (String) jComboBox.getSelectedItem();
        if (id == null)
            throw new NullPointerException();
        return id;
    }
}
