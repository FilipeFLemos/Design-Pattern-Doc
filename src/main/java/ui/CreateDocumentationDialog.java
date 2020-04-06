package ui;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.ValidationInfo;
import models.DesignPattern;
import models.PatternInstance;
import org.jetbrains.annotations.Nullable;
import storage.PluginState;
import utils.Utils;

import javax.swing.*;
import java.util.*;

public class CreateDocumentationDialog extends DocumentationDialog {

    private ComboBox patternNameComboBox;
    private Map<String, DesignPattern> designPatternByName;

    private String classParticipant;

    public CreateDocumentationDialog(boolean canBeParent, String className) {
        super(canBeParent);
        this.classParticipant = className;
        setDesignPatternByName();
        setPatternNameComboBox();
        setPatternNameComboBoxListener();
        setNumCollaborationRows(MIN_NUM_ROWS);
        setTitle("Document Pattern Instance");
        init();
    }

    public CreateDocumentationDialog(boolean canBeParent) {
        this(canBeParent, "");
    }

    @Override
    protected Set<String> getSelectedPatternRoles() {
        String selectedPatternName = (String) patternNameComboBox.getSelectedItem();
        DesignPattern selectedDesignPattern = designPatternByName.get(selectedPatternName);
        return selectedDesignPattern.getRoles();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        addRowElementToPanel(Utils.getFieldLabel("Pattern Name"));
        addRowElementToPanel(patternNameComboBox);
        addDocumentationDialogInvariableBody();
        fillFirstCollaborationRowWithClassName();

        return panel;
    }

    private void fillFirstCollaborationRowWithClassName() {
        CollaborationRowItem collaborationRowItem = collaborationRowList.get(0);
        JTextField className = collaborationRowItem.getClassName();
        className.setText(classParticipant);
    }

    private void setDesignPatternByName() {
        designPatternByName = new HashMap<>();
        Set<DesignPattern> supportedDesignPatterns = PluginState.getInstance().getSupportedDesignPatterns();
        for (DesignPattern designPattern : supportedDesignPatterns) {
            designPatternByName.put(designPattern.getName(), designPattern);
        }
    }

    private void setPatternNameComboBox() {
        Set<String> supportedDesignPatterns = designPatternByName.keySet();
        String[] designPatterns = new String[supportedDesignPatterns.size()];
        int index = 0;
        for (String designPatternName : supportedDesignPatterns) {
            designPatterns[index] = designPatternName;
            index++;
        }
        patternNameComboBox = new ComboBox(designPatterns);
    }

    private void setPatternNameComboBoxListener() {
        patternNameComboBox.addActionListener(e ->
        {
            ArrayList<String> filledClassNames = new ArrayList<>();
            for (CollaborationRowItem listItem : collaborationRowList) {
                String className = listItem.getClassName().getText();
                filledClassNames.add(className);
            }
            int numRows = collaborationRowList.size();
            removeAllCollaborationRoles();
            setNumCollaborationRows(numRows);
            addCollaborationListToPanel();
            changeDeleteBtnVisibilityWhenMinNumRows(false);

            for (int i = 0; i < filledClassNames.size(); i++) {
                String className = filledClassNames.get(i);
                CollaborationRowItem listItem = collaborationRowList.get(i);
                JTextField jTextField = listItem.getClassName();
                jTextField.setText(className);
            }
            updateCollaborationPanel();
        });
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        ValidationInfo commonValidationInfo = getCommonValidationInfo();
        if (failedCommonValidation(commonValidationInfo)) {
            return commonValidationInfo;
        }

        String name = (String) patternNameComboBox.getSelectedItem();
        PatternInstance patternInstance = generatePatternInstanceFromUserInput(name);
        if (projectPersistedState.hasAlreadyStored(patternInstance)) {
            return new ValidationInfo("This pattern instance has already been documented. Consider editing the existing one.");
        }

        return null;
    }

    @Override
    protected void doOKAction() {
        String name = (String) patternNameComboBox.getSelectedItem();
        PatternInstance patternInstance = generatePatternInstanceFromUserInput(name);
        storePatternInstance(patternInstance);
        updatePatternSuggestions(patternInstance);
        close(OK_EXIT_CODE);
    }

    private void storePatternInstance(PatternInstance patternInstance) {
        String id = Utils.generatePatternInstanceId(patternInstanceById);
        projectPersistedState.storePatternInstanceIfAbsent(id, patternInstance);
    }
}
