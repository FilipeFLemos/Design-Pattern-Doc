package ui;

import com.intellij.openapi.ui.ValidationInfo;
import detection.PatternSuggestions;
import models.PatternInstance;
import org.jetbrains.annotations.Nullable;
import storage.PluginState;
import utils.Utils;

import javax.swing.*;
import java.awt.*;

public class CreateDocumentationDialog extends DocumentationDialog {

    public CreateDocumentationDialog(boolean canBeParent) {
        super(canBeParent);
        setTitle("Document Pattern Instance");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        panel.setPreferredSize(new Dimension(500, 200));
        setNumCollaborationRows(DEFAULT_NUM_ROWS);
        addDocumentationDialogInvariableBody();

        return panel;
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        ValidationInfo commonValidationInfo = getCommonValidationInfo();
        if(failedCommonValidation(commonValidationInfo)){
            return commonValidationInfo;
        }

        PatternInstance patternInstance = generatePatternInstanceFromUserInput();
        if(projectPersistedState.hasAlreadyStored(patternInstance)){
            return new ValidationInfo("This pattern instance has already been documented. Consider editing the existing one.");
        }

        return null;
    }

    @Override
    protected void doOKAction() {
        PatternInstance patternInstance = generatePatternInstanceFromUserInput();
        storePatternInstance(patternInstance);
        updatePatternSuggestions(patternInstance);
        close(OK_EXIT_CODE);
    }

    private void storePatternInstance(PatternInstance patternInstance) {
        String id = Utils.generatePatternInstanceId(patternInstanceById);
        projectPersistedState.storePatternInstanceIfAbsent(id, patternInstance);
    }
}
