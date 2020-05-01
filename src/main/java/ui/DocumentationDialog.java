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
import utils.PlantUmlHelper;
import utils.Utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class DocumentationDialog extends DialogWrapper {

    protected JPanel panel, collaborationPanel, umlImagePanel;
    protected JTextArea patternIntent;
    protected JLabel pictureLabel;
    protected JButton addCollaborationRowBtn, zoomUML;
    protected ArrayList<CollaborationRowItem> collaborationRowList;
    protected JBScrollPane rolesScrollPane, intentScrollPane;
    protected ImageIcon umlImageIcon;
    protected int numCollaborationRows, gridHeight, collaborationGridHeight;
    private ArrayList<String> validFileNames;
    private Timer t;

    protected ProjectPersistedState projectPersistedState;
    protected ConcurrentHashMap<String, PatternInstance> patternInstanceById;
    protected ProjectDetails projectDetails;

    protected int MIN_NUM_ROWS = 3, DIALOG_WIDTH = 400,  INTENT_HEIGHT = 50, UML_HEIGHT = 200, ROLES_PANEL_HEIGHT = 140;

    public DocumentationDialog(boolean canBeParent) {
        super(canBeParent);

        panel = new JPanel(new GridBagLayout());
        patternIntent = new JTextArea();
        patternIntent.setLineWrap(true);
        intentScrollPane = new JBScrollPane(patternIntent);
        intentScrollPane.setPreferredSize(new Dimension(DIALOG_WIDTH, INTENT_HEIGHT));

        pictureLabel = new JLabel("");
        umlImagePanel = new JPanel(new FlowLayout());
        umlImagePanel.setPreferredSize(new Dimension(DIALOG_WIDTH, UML_HEIGHT));
        umlImagePanel.add(pictureLabel);

        addCollaborationRowBtn = new JButton("Add Pattern Role");

        collaborationPanel = new JPanel(new GridBagLayout());
        rolesScrollPane = new JBScrollPane(collaborationPanel);
        rolesScrollPane.setPreferredSize(new Dimension(DIALOG_WIDTH, ROLES_PANEL_HEIGHT));
        rolesScrollPane.createVerticalScrollBar();

        collaborationRowList = new ArrayList<>();
        gridHeight = 0;
        collaborationGridHeight = 0;

        setResizable(false);
        setAddCollaborationRowBtnListener();

        try {
            setProjectState();
            setPatternInstanceById();
            projectDetails = PluginState.getInstance().getProjectDetails();
            Set<String> fileNames = projectDetails.getAllFileNamesFromProject();
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

    protected abstract String getPatternName();

    protected void addDocumentationDialogInvariableBody() {
        addRowElementToPanel(Utils.getFieldLabel("Intent"));
        addRowElementToPanel(intentScrollPane);
        addCollaborationHeaderToPanel();
        addRowElementToPanel(rolesScrollPane);
        addCollaborationListToPanel();
        changeDeleteBtnVisibilityWhenMinNumRows(false);
        addRowElementToPanel(umlImagePanel);
        addUMLZoomBtnToPanel();
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
        char rightArrow = '\u2192';
        String arrowIcon = " " + rightArrow + " ";
        panel.add(Utils.getFieldLabel("Collaborations (Class" + arrowIcon + "Role)"), c);

        c.anchor = GridBagConstraints.EAST;
        c.gridwidth = 1;
        c.gridx = 3;
        panel.add(addCollaborationRowBtn, c);

        gridHeight++;
    }

    private void addUMLZoomBtnToPanel() {
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = 4;
        c.gridx = 0;
        c.gridy = gridHeight;
        c.anchor = GridBagConstraints.WEST;

        zoomUML = new JButton("Zoom UML");
        zoomUML.setVisible(false);
        setZoomUMLBtnListener();
        JPanel umlBtnPanel = new JPanel(new FlowLayout());
        umlBtnPanel.setPreferredSize(new Dimension(100,40));
        umlBtnPanel.add(zoomUML);

        panel.add(umlBtnPanel, c);
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
        setObjectTextFieldListener(object);

        c.weightx = 0.0;
        c.gridx = 1;
        char rightArrow = '\u2192';
        String arrowIcon = " " + rightArrow + " ";
        JLabel arrow = Utils.getFieldLabel(arrowIcon);
        collaborationPanel.add(arrow, c);

        c.weightx = 0.5;
        c.gridx = 2;
        ComboBox roleComboBox = getRoleComboBox(roles);
        setRoleComboBoxListener(roleComboBox);
        collaborationPanel.add(roleComboBox, c);

        c.weightx = 0.0;
        c.gridx = 3;
        char xIcon = '\u2717';
        JButton deleteRowBtn = new JButton(xIcon + "");
        collaborationPanel.add(deleteRowBtn, c);

        CollaborationRowItem listItem = new CollaborationRowItem(object, roleComboBox, arrow, deleteRowBtn);
        collaborationRowList.add(listItem);
        collaborationGridHeight++;

        deleteRowBtn.addActionListener(e -> {
            removeCollaborationRowObjects(listItem);
            collaborationRowList.remove(listItem);
            changeDeleteBtnVisibilityWhenMinNumRows(false);
            updateCollaborationPanel();
            redrawUML();
        });
    }

    private void setRoleComboBoxListener(ComboBox roleComboBox) {
        roleComboBox.addActionListener(e->{
            if(t == null){
                initTimer();
            }
            else{
                t.restart();
            }
        });
    }

    private void setObjectTextFieldListener(JTextField object) {
        object.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                setTimer();
            }
            public void removeUpdate(DocumentEvent e) {
                setTimer();
            }
            public void insertUpdate(DocumentEvent e) {
                setTimer();
            }

            public void setTimer(){
                if(t == null){
                    initTimer();
                }
                else{
                    t.restart();
                }
            }
        });
    }

    private void initTimer() {
        t = new Timer(1000, e-> redrawUML());
        t.setRepeats(false);
    }

    protected void changeDeleteBtnVisibilityWhenMinNumRows(boolean b) {
        if (collaborationRowList.size() == 1) {
            CollaborationRowItem collaborationRowItem = collaborationRowList.get(0);
            JButton deleteBtn = collaborationRowItem.getjButton();
            deleteBtn.setVisible(b);
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
            JScrollBar vertical = rolesScrollPane.getVerticalScrollBar();
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

        Set<PatternParticipant> patternParticipants = new HashSet<>();

        for (CollaborationRowItem listItem : collaborationRowList) {
            String object = listItem.getClassName().getText();
            String role = (String) listItem.getRole().getSelectedItem();
            if(object.equals("")){
                continue;
            }

            patternParticipants.add(new PatternParticipant(object, role));
        }

        Set<String> roles = getSelectedPatternRoles();

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

    protected void redrawUML() {
        String name = getPatternName();
        PatternInstance patternInstance = generatePatternInstanceFromUserInput(name);
        String path = new PlantUmlHelper(patternInstance).getUmlFilePath();
        try {
            ImageIcon resizedUmlImageIcon = getResizedImageIcon(path);
            pictureLabel.setIcon(resizedUmlImageIcon);
            zoomUML.setVisible(true);
            umlImageIcon = new ImageIcon(path);
            umlImagePanel.setBackground(Color.WHITE);
            updatePanel();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @NotNull
    private ImageIcon getResizedImageIcon(String path) throws IOException {
        File file = new File(path);
        BufferedImage image = ImageIO.read(file);
        int imageWidth = Math.min(image.getWidth(), DIALOG_WIDTH - 10);
        int imageHeight = Math.min(image.getHeight(), UML_HEIGHT);

        Image resizedImage = image.getScaledInstance(imageWidth, imageHeight, Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImage);
    }

    private void setZoomUMLBtnListener(){
        zoomUML.addActionListener(e -> JOptionPane.showMessageDialog(panel, "", "UML Preview", JOptionPane.INFORMATION_MESSAGE, umlImageIcon));
    }
}
