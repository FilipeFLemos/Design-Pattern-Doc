package ui;

import models.PatternInstance;
import org.jetbrains.annotations.Nullable;
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

    @Override
    protected void doOKAction() {
        PatternInstance patternInstance = generatePatternInstanceFromUserInput();
        String id = Utils.generatePatternInstanceId(patternInstanceById);
        projectPersistedState.storePatternInstanceIfAbsent(id, patternInstance);
        close(OK_EXIT_CODE);
    }
}
