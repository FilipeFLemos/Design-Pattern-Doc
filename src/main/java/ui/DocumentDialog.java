package ui;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import models.CollaborationListItem;
import models.PatternInstance;
import models.Relation;
import org.jetbrains.annotations.Nullable;
import storage.PersistentState;
import storage.PluginState;
import utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DocumentDialog extends DialogWrapper {

    private JPanel panel;
    private JTextField patternName;
    private JTextArea patternIntent;
    private JButton addCollabRowBtn;
    private ArrayList<CollaborationListItem> collaborationList;
    private ComboBox jComboBox;
    private JButton deletePatternInstance;

    private int gridHeight = 0;
    private boolean editingDocumentation;
    private final int DEFAULT_NUM_ROWS = 3;

    public DocumentDialog(boolean canBeParent, boolean editingDocumentation) {
        super(canBeParent);

        panel = new JPanel(new GridBagLayout());
        patternName = new JTextField();
        patternIntent = new JTextArea();
        patternIntent.setLineWrap(true);
        addCollabRowBtn = new JButton("Add Row");
        collaborationList = new ArrayList<>();

        this.editingDocumentation = editingDocumentation;

        init();

        if(editingDocumentation){
            setTitle("Edit Pattern Instance Documentation");
        }
        else {
            setTitle("Document Pattern Instance");
        }
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {

        panel.setPreferredSize(new Dimension(500,200));

        int numOfCollaborationRows = DEFAULT_NUM_ROWS;

        if(editingDocumentation){
            PersistentState persistentState = (PersistentState) PluginState.getInstance().getState();
            ConcurrentHashMap<String, PatternInstance> patternInstanceById = persistentState.getPatternInstanceById();

            setJComboBox(patternInstanceById);
            setJComboListener(patternInstanceById);

            String id = (String) jComboBox.getSelectedItem();
            numOfCollaborationRows = patternInstanceById.get(id).getCollaborationRows().size();

            addElementToPanel(getLabel("Stored Pattern Instances"));
            addPatternInstancesHeaderToPanel();
        }

        addElementToPanel(getLabel("Pattern Name"));
        addElementToPanel(patternName);
        addElementToPanel(getLabel("Intent"));
        addElementToPanel(patternIntent);
        addCollaborationHeaderToPanel();
        addCollaborationListToPanel(numOfCollaborationRows);

        addCollabRowBtn.addActionListener(e -> {
            addCollaborationRowToPanel();
            panel.revalidate();
        });


        if(editingDocumentation){
            fillFields();
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
        deletePatternInstance.addActionListener(e -> {
            PersistentState persistentState = (PersistentState) PluginState.getInstance().getState();

            String id = (String) jComboBox.getSelectedItem();
            persistentState.deletePatternInstance(id);

            close(NEXT_USER_EXIT_CODE);
        });
    }

    private void setJComboListener(ConcurrentHashMap<String, PatternInstance> patternInstanceById) {
        jComboBox.addActionListener(e->{
            String id = (String) jComboBox.getSelectedItem();
            int numRows = patternInstanceById.get(id).getCollaborationRows().size();
            removeCollaborationRoles();
            addCollaborationListToPanel(numRows);
            fillFields();
            panel.revalidate();
            panel.repaint();
        });
    }

    private void setJComboBox(ConcurrentHashMap<String, PatternInstance> patternInstanceById) {
        String[] patternInstancesIds = new String[patternInstanceById.size()];
        int index = 0;
        for (Map.Entry<String, PatternInstance> entry : patternInstanceById.entrySet()) {
            patternInstancesIds[index] = entry.getKey();
            index++;
        }
        jComboBox = new ComboBox(patternInstancesIds);
    }

    private void addCollaborationListToPanel(int numOfCollaborationRows) {
        for (int i = 0; i < numOfCollaborationRows; i++) {
            addCollaborationRowToPanel();
        }
    }


    private void fillFields() {
        PersistentState persistentState = (PersistentState) PluginState.getInstance().getState();

        String id = (String) jComboBox.getSelectedItem();
        PatternInstance patternInstance = persistentState.getPatternInstance(id);

        patternName.setText(patternInstance.getPatternName());
        patternIntent.setText(patternInstance.getIntent());

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

    private void addElementToPanel(JComponent jComponent){
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 4;
        c.gridx = 0;
        c.gridy = gridHeight;
        c.insets = JBUI.insetsBottom(5);
        panel.add(jComponent, c);

        gridHeight++;
    }

    private void addCollaborationHeaderToPanel(){
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 3;
        c.gridx = 0;
        c.gridy = gridHeight;
        c.insets = JBUI.insets(5,0,5,0);
        panel.add(getLabel("Collaborations (Class -> Role)"), c);

        c.gridx = 3;
        panel.add(addCollabRowBtn,c);

        gridHeight++;
    }

    private void addCollaborationRowToPanel(){
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        JTextField object = new JTextField();
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = gridHeight;
        panel.add(object, c);

        c.weightx = 0.0;
        c.gridx = 1;
        JLabel arrow = getLabel("->");
        panel.add(arrow, c);

        JTextField role = new JTextField();
        c.weightx = 1.0;
        c.gridx = 2;
        panel.add(role, c);

        JButton deleteRowBtn = new JButton("X");
        c.weightx = 0.0;
        c.gridx = 3;
        panel.add(deleteRowBtn,c);

        CollaborationListItem listItem = new CollaborationListItem(object, role, arrow, deleteRowBtn);
        collaborationList.add(listItem);
        gridHeight++;

        deleteRowBtn.addActionListener(e -> {
            removeCollaborationRowObjects(object,role,arrow,deleteRowBtn);
            panel.revalidate();
            panel.repaint();
            gridHeight--;
            collaborationList.remove(listItem);
        });
    }

    private void removeCollaborationRoles(){
        for(CollaborationListItem listItem : collaborationList){
            JTextField className = listItem.getClassName();
            JTextField role = listItem.getRole();
            JLabel jLabel = listItem.getjLabel();
            JButton jButton = listItem.getjButton();

            removeCollaborationRowObjects(className, role, jLabel, jButton);
        }

        gridHeight -= collaborationList.size();
        collaborationList = new ArrayList<>();

    }

    private void removeCollaborationRowObjects(JTextField className, JTextField role, JLabel jLabel, JButton jButton) {
        panel.remove(className);
        panel.remove(role);
        panel.remove(jLabel);
        panel.remove(jButton);
    }


    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        String name = patternName.getText();
        if(name.equals("")){
            return new ValidationInfo("This field is mandatory!", patternName);
        }

        if(collaborationList.isEmpty()){
            return new ValidationInfo("There must be at least one collaboration row!", addCollabRowBtn);
        }

        for(CollaborationListItem listItem : collaborationList){
            String className = listItem.getClassName().getText();
            String role = listItem.getRole().getText();

            if(className.equals("")){
                return new ValidationInfo("This field is mandatory! If you do not need the row, consider removing it.", listItem.getClassName());
            }
            else if(role.equals("")){
                return new ValidationInfo("This field is mandatory! If you do not need the row, consider removing it.", listItem.getRole());
            }
        }

        return null;
    }

    @Override
    protected void doOKAction() {
        String name = patternName.getText();
        String intent = patternIntent.getText();

        Map<String, Set<String>> roleObjects = new HashMap<>();
        Map<String, Set<String>> objectRoles = new HashMap<>();
        for(CollaborationListItem listItem : collaborationList){
            String className = listItem.getClassName().getText();
            String role = listItem.getRole().getText();

            Set<String> roles = new HashSet<>();
            if(objectRoles.containsKey(className)){
                roles = objectRoles.get(className);
            }

            roles.add(role);
            objectRoles.put(className, roles);

            Set<String> objects = new HashSet<>();
            if(roleObjects.containsKey(role)){
                objects = roleObjects.get(role);
            }

            objects.add(className);
            roleObjects.put(role, objects);
        }

        PatternInstance patternInstance = new PatternInstance(name, intent, roleObjects, objectRoles);

        PersistentState persistentState = (PersistentState) PluginState.getInstance().getState();
        ConcurrentHashMap<String, PatternInstance> patternInstanceById = persistentState.getPatternInstanceById();


        if(editingDocumentation){
            String id = (String) jComboBox.getSelectedItem();
            persistentState.updatePatternInstance(id, patternInstance);
        }else {
            String id = generatePatternInstanceId(patternInstanceById);
            persistentState.storePatternInstanceIfAbsent(id, patternInstance);
        }

        close(OK_EXIT_CODE);
    }


    private String generatePatternInstanceId(ConcurrentHashMap<String, PatternInstance> patternInstanceById) {
        String id;
        do {
            id = Utils.generateAlphaNumericString();
        } while (patternInstanceById.containsKey(id));

        return id;
    }

    private JBLabel getLabel(String text){
        JBLabel jLabel = new JBLabel(text);
        jLabel.setComponentStyle(UIUtil.ComponentStyle.SMALL);
        jLabel.setFontColor(UIUtil.FontColor.BRIGHTER);
        jLabel.setBorder(JBUI.Borders.empty(0,5,2,0));
        return jLabel;
    }
}
