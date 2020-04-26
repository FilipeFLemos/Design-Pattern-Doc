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
import javax.swing.plaf.basic.BasicArrowButton;
import java.awt.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class EditDocumentationDialog extends DocumentationDialog {

    private JButton deletePatternInstance;
    private JLabel selectedPatternName, numberOfInstances;
    private ArrayList<String> patternInstancesIds;
    private int currentIndex;
    private BasicArrowButton leftArrow, rightArrow;

    public EditDocumentationDialog(boolean canBeParent) {
        super(canBeParent);

        setPatternInstanceSlider();
        setSelectedPatternInstanceNumCollaborationRows();

        deletePatternInstance = new JButton("DELETE");
        setDeletePatternInstanceDocListener();
        setTitle("Edit Pattern Instance Documentation");
        init();
    }

    public EditDocumentationDialog(boolean canBeParent, String className) {
        this(canBeParent);
        setPatternInstanceSlider(className);
        updatePanelOnPatternInstanceChange();
    }

    @Override
    protected Set<String> getSelectedPatternRoles() {
        PatternInstance selectedPatternInstance = getSelectedPatternInstance();
        return selectedPatternInstance.getRoleObjects().keySet();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
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

    private void setPatternInstanceSlider() {
        patternInstancesIds = new ArrayList<>(patternInstanceById.keySet());
        currentIndex = 0;
    }

    private void setPatternInstanceSlider(String object) {
        patternInstancesIds = new ArrayList<>();
        for(Map.Entry<String, PatternInstance> entry : patternInstanceById.entrySet()){
            String id = entry.getKey();
            PatternInstance patternInstance = entry.getValue();
            Set<String> objects = patternInstance.getObjectRoles().keySet();

            if (objects.contains(object)) {
                patternInstancesIds.add(id);
            }
        }
        currentIndex = 0;
    }

    private void updatePanelOnPatternInstanceChange(){
        setSelectedPatternInstanceNumCollaborationRows();
        numberOfInstances.setText("    " + (currentIndex + 1) + "/" + patternInstancesIds.size());
        hidePatternSlider();

        removeAllCollaborationRoles();
        addCollaborationListToPanel();
        fillFields();
        updatePanel();
    }

    private void setSelectedPatternInstanceNumCollaborationRows() {
        String id = getSelectedPatternInstanceId();
        int numRows = patternInstanceById.get(id).getPatternParticipants().size();
        setNumCollaborationRows(numRows);
    }

    private void addPatternInstancesHeaderToPanel() {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weighty = 1;
        c.gridy = gridHeight;
        c.insets = JBUI.insets(0,5,5,0);

        leftArrow = new BasicArrowButton(BasicArrowButton.WEST);
        leftArrow.addActionListener(e ->
        {
            currentIndex--;
            if(currentIndex < 0){
                currentIndex = patternInstancesIds.size() - 1;
            }
            updatePanelOnPatternInstanceChange();
        });
        panel.add(leftArrow, c);

        String labelValue = "    " + (currentIndex + 1) + "/" + patternInstancesIds.size();
        numberOfInstances = Utils.getFieldLabel(labelValue);
        c.gridx = 1;
        panel.add(numberOfInstances, c);

        rightArrow = new BasicArrowButton(BasicArrowButton.EAST);
        rightArrow.addActionListener(e ->
        {
            currentIndex++;
            if(currentIndex >= patternInstancesIds.size()){
                currentIndex = 0;
            }
            updatePanelOnPatternInstanceChange();
        });
        c.gridx = 2;
        c.insets = JBUI.insets(0,-25,5,0);
        panel.add(rightArrow, c);

        hidePatternSlider();

        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.EAST;
        c.gridx = 3;
        panel.add(deletePatternInstance, c);

        gridHeight++;
    }

    private void hidePatternSlider() {
        if(patternInstancesIds.size() == 1){
            rightArrow.setVisible(false);
            leftArrow.setVisible(false);
            numberOfInstances.setVisible(false);
        }
    }

    private void setDeletePatternInstanceDocListener() {
        deletePatternInstance.addActionListener(e ->
        {
            String id = getSelectedPatternInstanceId();
            PatternInstance patternInstance = getSelectedPatternInstance();
            removePatternInstanceFromSuggestions(patternInstance);
            projectPersistedState.deletePatternInstance(id);
            PluginState.getInstance().restartHighlighting();
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
        String id = getSelectedPatternInstanceId();
        String name = selectedPatternName.getText();
        PatternInstance patternInstance = generatePatternInstanceFromUserInput(name);
        updatePatternInstance(patternInstance);
        updatePatternSuggestions(patternInstance);
        updatePatternInstanceUml(id);
        close(OK_EXIT_CODE);
    }

    private void updatePatternInstance(PatternInstance patternInstance) {
        String id = patternInstancesIds.get(currentIndex);
        projectPersistedState.updatePatternInstance(id, patternInstance);
    }

    private String getSelectedPatternInstanceId(){
        return patternInstancesIds.get(currentIndex);
    }

    private PatternInstance getSelectedPatternInstance() {
        String id = patternInstancesIds.get(currentIndex);
        return projectPersistedState.getPatternInstance(id);
    }
}

