package ui;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.util.ui.JBUI;
import detection.PatternSuggestions;
import models.PatternInstance;
import models.PatternParticipant;
import org.jetbrains.annotations.Nullable;
import storage.PluginState;
import utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class EditDocumentationDialog extends DocumentationDialog {

    private ComboBox patternInstanceComboBox;
    private JButton deletePatternInstance;
    private JLabel selectedPatternName;

    public EditDocumentationDialog(boolean canBeParent) {
        super(canBeParent);

        setPatternInstanceComboBox();
        setPatternInstanceComboListener();
        setSelectedPatternInstanceNumCollaborationRows();

        deletePatternInstance = new JButton("DELETE");
        setDeletePatternInstanceDocListener();
        setTitle("Edit Pattern Instance Documentation");
        init();
    }

    public EditDocumentationDialog(boolean canBeParent, String patternInstanceId) {
        this(canBeParent);
        patternInstanceComboBox.setSelectedItem(patternInstanceId);
    }

    @Override
    protected Set<String> getSelectedPatternRoles() {
        PatternInstance selectedPatternInstance = getSelectedPatternInstance();
        return selectedPatternInstance.getRoleObjects().keySet();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        addRowElementToPanel(Utils.getFieldLabel("Stored Pattern Instances"));
        addPatternInstancesHeaderToPanel();
        addRowElementToPanel(Utils.getFieldLabel("Pattern Name"));
        addPatternNameToPanel();
        addDocumentationDialogInvariableBody();

        fillFields();

        return panel;
    }

    private void addPatternNameToPanel() {
        selectedPatternName = new JLabel();
        selectedPatternName.setBorder(JBUI.Borders.empty(0, 5, 2, 0));
        PatternInstance patternInstance = getSelectedPatternInstance();
        selectedPatternName.setText(patternInstance.getPatternName());
        addRowElementToPanel(selectedPatternName);
    }

    private void setPatternInstanceComboBox() {
        String[] patternInstancesIds = new String[patternInstanceById.size()];
        int index = 0;
        for (Map.Entry<String, PatternInstance> entry : patternInstanceById.entrySet()) {
            patternInstancesIds[index] = entry.getKey();
            index++;
        }
        patternInstanceComboBox = new ComboBox(patternInstancesIds);
    }

    private void setPatternInstanceComboListener() {
        patternInstanceComboBox.addActionListener(e ->
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

    private void addPatternInstancesHeaderToPanel() {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 3;
        c.weightx = 1.0;
        c.gridx = 0;
        c.gridy = gridHeight;
        c.insets = JBUI.insetsBottom(5);
        c.ipadx = 200;
        panel.add(patternInstanceComboBox, c);

        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.EAST;
        c.gridwidth = 1;
        c.gridx = 3;
        c.ipadx = 0;
        panel.add(deletePatternInstance, c);

        gridHeight++;
    }

    private void setDeletePatternInstanceDocListener() {
        deletePatternInstance.addActionListener(e ->
        {
            String id = getSelectedPatternInstanceId();
            PatternInstance patternInstance = getSelectedPatternInstance();
            removePatternInstanceFromSuggestions(patternInstance);
            projectPersistedState.deletePatternInstance(id);
            close(NEXT_USER_EXIT_CODE);
        });
    }

    private void removePatternInstanceFromSuggestions(PatternInstance patternInstance) {
        PatternSuggestions patternSuggestions = PluginState.getInstance().getPatternSuggestions();
        patternSuggestions.updateSuggestionsAfterPatternInstanceDeletion(patternInstance);
    }

    private void fillFields() {
        String id = getSelectedPatternInstanceId();
        PatternInstance patternInstance = projectPersistedState.getPatternInstance(id);

        selectedPatternName.setText(patternInstance.getPatternName());
        patternIntent.setText(patternInstance.getIntent());
        fillCollaborationRows(patternInstance);
    }

    private void fillCollaborationRows(PatternInstance patternInstance) {
        ArrayList<PatternParticipant> patternParticipants = new ArrayList<>(patternInstance.getPatternParticipants());
        int index = 0;
        for (CollaborationRowItem listItem : collaborationRowList) {
            JTextField classNameField = listItem.getClassName();
            ComboBox roleField = listItem.getRole();

            PatternParticipant patternParticipant = patternParticipants.get(index);
            String className = patternParticipant.getObject();
            String role = patternParticipant.getRole();

            classNameField.setText(className);
            roleField.setSelectedItem(role);

            index++;
        }
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        ValidationInfo commonValidationInfo = getCommonValidationInfo();
        if (failedCommonValidation(commonValidationInfo)) {
            return commonValidationInfo;
        }

        String id = getSelectedPatternInstanceId();
        String name = selectedPatternName.getText();
        PatternInstance patternInstance = generatePatternInstanceFromUserInput(name);
        if (existsOtherDocumentationForPatternInstance(id, patternInstance)) {
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
        String name = selectedPatternName.getText();
        PatternInstance patternInstance = generatePatternInstanceFromUserInput(name);
        updatePatternInstance(patternInstance);
        updatePatternSuggestions(patternInstance);
        close(OK_EXIT_CODE);
    }

    private void updatePatternInstance(PatternInstance patternInstance) {
        String id = getSelectedPatternInstanceId();
        projectPersistedState.updatePatternInstance(id, patternInstance);
    }

    private String getSelectedPatternInstanceId() throws NullPointerException {
        String id = (String) patternInstanceComboBox.getSelectedItem();
        if (id == null)
            throw new NullPointerException();
        return id;
    }

    private PatternInstance getSelectedPatternInstance() {
        String id = getSelectedPatternInstanceId();
        return projectPersistedState.getPatternInstance(id);
    }
}

