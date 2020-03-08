package ui;

import models.PatternInstance;
import org.jetbrains.annotations.Nullable;
import utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ConcurrentHashMap;

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

    private String generatePatternInstanceId(ConcurrentHashMap<String, PatternInstance> patternInstanceById) {
        String id;
        do {
            id = Utils.generateAlphaNumericString();
        } while (patternInstanceById.containsKey(id));

        return id;
    }

    @Override
    protected void doOKAction() {
        PatternInstance patternInstance = generatePatternInstanceFromUserInput();
        String id = generatePatternInstanceId(patternInstanceById);
        persistentState.storePatternInstanceIfAbsent(id, patternInstance);
        close(OK_EXIT_CODE);
    }
}
