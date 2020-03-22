package ui;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.util.ui.JBUI;
import models.CollaborationListItem;
import models.PatternInstance;
import models.PatternParticipant;
import org.jetbrains.annotations.Nullable;
import utils.Utils;

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

            addRowElementToPanel(getLabel("Stored Pattern Instances"));
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
            projectPersistedState.deletePatternInstance(id);
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
        int numRows = patternInstanceById.get(id).getPatternParticipants().size();
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
        PatternInstance patternInstance = projectPersistedState.getPatternInstance(id);

        patternName.setText(patternInstance.getPatternName());
        patternIntent.setText(patternInstance.getIntent());
        fillCollaborationRows(patternInstance);
    }

    private void fillCollaborationRows(PatternInstance patternInstance) {
        ArrayList<PatternParticipant> patternParticipants = new ArrayList<>(patternInstance.getPatternParticipants());
        int index = 0;
        for(CollaborationListItem listItem : collaborationList){
            JTextField classNameField = listItem.getClassName();
            JTextField roleField = listItem.getRole();

            PatternParticipant patternParticipant = patternParticipants.get(index);
            String className = patternParticipant.getObject();
            String role = patternParticipant.getRole();

            classNameField.setText(className);
            roleField.setText(role);

            index++;
        }
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        ValidationInfo commonValidationInfo = getCommonValidationInfo();
        if(failedCommonValidation(commonValidationInfo)){
            return commonValidationInfo;
        }

        String id = getSelectedPatternInstanceId();
        PatternInstance patternInstance = generatePatternInstanceFromUserInput();
        if(existsOtherDocumentationForPatternInstance(id, patternInstance)){
            return new ValidationInfo("Your edition has result in a new pattern instance, which has already been documented. Consider editing the existing one.");
        }

        return null;
    }

    private boolean existsOtherDocumentationForPatternInstance(String id, PatternInstance patternInstance) {
        boolean existsPatternInstance = false;

        for (Map.Entry<String, PatternInstance> entry : patternInstanceById.entrySet()) {
            String persistedPatternInstanceId = entry.getKey();
            PatternInstance persistedPatternInstance = entry.getValue();
            if (patternInstance.areTheSamePatternInstance(persistedPatternInstance) && !id.equals(persistedPatternInstanceId)) {
                existsPatternInstance = true;
                break;
            }
        }
        return existsPatternInstance;
    }

    @Override
    protected void doOKAction() {
        PatternInstance patternInstance = generatePatternInstanceFromUserInput();
        updatePatternInstance(patternInstance);
        updatePatternSuggestions(patternInstance);
        close(OK_EXIT_CODE);
    }

    private void updatePatternInstance(PatternInstance patternInstance) {
        String id = getSelectedPatternInstanceId();
        projectPersistedState.updatePatternInstance(id, patternInstance);
    }

    private String getSelectedPatternInstanceId() throws NullPointerException {
        String id = (String) jComboBox.getSelectedItem();
        if (id == null)
            throw new NullPointerException();
        return id;
    }
}
