package ui;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import detection.PatternSuggestions;
import models.PatternInstance;
import models.PatternParticipant;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import storage.PluginState;
import storage.ProjectDetails;
import storage.ProjectPersistedState;
import utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class DocumentationDialog extends DialogWrapper {

    protected JPanel panel;
    protected JTextArea patternIntent;
    protected JButton addCollaborationRowBtn;
    protected ArrayList<CollaborationRowItem> collaborationRowList;
    protected JPanel collaborationPanel;
    protected JBScrollPane scrollPane;
    protected int numCollaborationRows;
    protected int gridHeight;
    protected int collaborationGridHeight;
    protected int MIN_NUM_ROWS;
    private ArrayList<String> validFileNames;

    protected ProjectPersistedState projectPersistedState;
    protected ConcurrentHashMap<String, PatternInstance> patternInstanceById;

    public DocumentationDialog(boolean canBeParent) {
        super(canBeParent);

        panel = new JPanel(new GridBagLayout());
        patternIntent = new JTextArea();
        patternIntent.setLineWrap(true);
        addCollaborationRowBtn = new JButton("Add Row");
        collaborationPanel = new JPanel(new GridBagLayout());

        scrollPane = new JBScrollPane(collaborationPanel);
        scrollPane.setPreferredSize(new Dimension(400, 100));
        scrollPane.createVerticalScrollBar();

        collaborationRowList = new ArrayList<>();
        gridHeight = 0;
        collaborationGridHeight = 0;

        setResizable(false);
        setAddCollaborationRowBtnListener();

        try {
            setProjectState();
            setPatternInstanceById();
            Set<String> fileNames = PluginState.getInstance().getProjectDetails().getAllFileNamesFromProject();
            validFileNames = new ArrayList<>(fileNames);
        } catch (Exception ignored) {

        }
    }

    protected abstract Set<String> getSelectedPatternRoles();

    @Nullable
    @Override
    protected abstract JComponent createCenterPanel();

    @Nullable
    @Override
    protected abstract ValidationInfo doValidate();

    protected void addDocumentationDialogInvariableBody() {
        addRowElementToPanel(Utils.getFieldLabel("Intent"));
        addRowElementToPanel(patternIntent);
        addCollaborationHeaderToPanel();
        addRowElementToPanel(scrollPane);
        addCollaborationListToPanel();
        changeDeleteBtnVisibilityWhenMinNumRows(false);
    }

    protected void addRowElementToPanel(JComponent jComponent) {
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
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = 3;
        c.gridx = 0;
        c.gridy = gridHeight;
        c.insets = JBUI.insets(5, 0, 5, 0);
        panel.add(Utils.getFieldLabel("Collaborations (Class -> Role)"), c);

        c.anchor = GridBagConstraints.EAST;
        c.gridwidth = 1;
        c.gridx = 3;
        panel.add(addCollaborationRowBtn, c);

        gridHeight++;
    }

    protected void addCollaborationListToPanel() {
        Set<String> roles = getSelectedPatternRoles();
        for (int i = 0; i < numCollaborationRows; i++) {
            addCollaborationRowToPanel(roles);
        }
    }

    protected void addCollaborationRowToPanel(Set<String> roles) {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = gridHeight;
        c.gridy = collaborationGridHeight;
        JTextField object = new JTextField();
        collaborationPanel.add(object, c);

        AutoCompleteDecorator.decorate(object, validFileNames, false);

        c.weightx = 0.0;
        c.gridx = 1;
        JLabel arrow = Utils.getFieldLabel("->");
        collaborationPanel.add(arrow, c);

        c.weightx = 0.5;
        c.gridx = 2;
        ComboBox roleComboBox = getRoleComboBox(roles);
        collaborationPanel.add(roleComboBox, c);

        c.weightx = 0.0;
        c.gridx = 3;
        JButton deleteRowBtn = new JButton("X");
        collaborationPanel.add(deleteRowBtn, c);

        CollaborationRowItem listItem = new CollaborationRowItem(object, roleComboBox, arrow, deleteRowBtn);
        collaborationRowList.add(listItem);
        collaborationGridHeight++;

        deleteRowBtn.addActionListener(e -> {
            removeCollaborationRowObjects(listItem);
            collaborationRowList.remove(listItem);
            changeDeleteBtnVisibilityWhenMinNumRows(false);
            updateCollaborationPanel();
        });
    }

    protected void changeDeleteBtnVisibilityWhenMinNumRows(boolean b) {
        if (collaborationRowList.size() == MIN_NUM_ROWS) {
            for (int i = 0; i < MIN_NUM_ROWS; i++) {
                CollaborationRowItem collaborationRowItem = collaborationRowList.get(i);
                JButton deleteBtn = collaborationRowItem.getjButton();
                deleteBtn.setVisible(b);
            }
        }
    }

    protected ComboBox getRoleComboBox(Set<String> selectedPatternRoles) {
        String[] roles = new String[selectedPatternRoles.size()];
        int index = 0;
        for (String role : selectedPatternRoles) {
            roles[index] = role;
            index++;
        }
        return new ComboBox(roles);
    }

    protected void setAddCollaborationRowBtnListener() {
        addCollaborationRowBtn.addActionListener(e -> {
            changeDeleteBtnVisibilityWhenMinNumRows(true);
            Set<String> roles = getSelectedPatternRoles();
            addCollaborationRowToPanel(roles);
            updateCollaborationPanel();
            scrollToBottomCollaborationPanel();
        });
    }

    private void scrollToBottomCollaborationPanel() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(Integer.MAX_VALUE);
        });
    }

    protected void updatePanel() {
        panel.revalidate();
        panel.repaint();
    }

    protected void updateCollaborationPanel() {
        collaborationPanel.revalidate();
        collaborationPanel.repaint();
    }

    protected void removeAllCollaborationRoles() {
        for (CollaborationRowItem listItem : collaborationRowList) {
            removeCollaborationRowObjects(listItem);
        }

        collaborationGridHeight -= collaborationRowList.size();
        collaborationRowList = new ArrayList<>();
    }

    protected void removeCollaborationRowObjects(CollaborationRowItem listItem) {
        JTextField className = listItem.getClassName();
        ComboBox role = listItem.getRole();
        JLabel jLabel = listItem.getjLabel();
        JButton jButton = listItem.getjButton();

        collaborationPanel.remove(className);
        collaborationPanel.remove(role);
        collaborationPanel.remove(jLabel);
        collaborationPanel.remove(jButton);
    }

    protected ValidationInfo getCommonValidationInfo() {
        if (collaborationRowList.isEmpty()) {
            return new ValidationInfo("There must be at least one collaboration row!", addCollaborationRowBtn);
        }

        for (CollaborationRowItem listItem : collaborationRowList) {
            String className = listItem.getClassName().getText();
            String role = (String) listItem.getRole().getSelectedItem();

            if (className.equals("")) {
                return new ValidationInfo("This field is mandatory! If you do not need the row, consider removing it.", listItem.getClassName());
            } else if (role == null || role.equals("")) {
                return new ValidationInfo("This field is mandatory! If you do not need the row, consider removing it.", listItem.getRole());
            }
        }

        return null;
    }

    protected boolean failedCommonValidation(ValidationInfo validationInfo) {
        return validationInfo != null;
    }

    @NotNull
    protected PatternInstance generatePatternInstanceFromUserInput(String patternName) {
        String intent = patternIntent.getText();

        Set<String> roles = new HashSet<>();
        Set<PatternParticipant> patternParticipants = new HashSet<>();

        for (CollaborationRowItem listItem : collaborationRowList) {
            String object = listItem.getClassName().getText();
            String role = (String) listItem.getRole().getSelectedItem();

            roles.add(role);
            patternParticipants.add(new PatternParticipant(object, role));
        }

        return new PatternInstance(patternName, intent, roles, patternParticipants);
    }

    protected void updatePatternSuggestions(PatternInstance patternInstance) {
        PatternSuggestions patternSuggestions = PluginState.getInstance().getPatternSuggestions();
        patternSuggestions.updateSuggestionsAfterManualDocumentation(patternInstance);
        PluginState.getInstance().restartHighlighting();
    }

    protected void setProjectState() throws NullPointerException {
        ProjectDetails projectDetails = PluginState.getInstance().getProjectDetails();
        this.projectPersistedState = projectDetails.getActiveProjectPersistedState();
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
