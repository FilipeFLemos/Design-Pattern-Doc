package ui;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import models.CollaborationListItem;
import models.PatternInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public abstract class DocumentationDialog extends DialogWrapper {

    protected JPanel panel;
    protected JTextField patternName;
    protected JTextArea patternIntent;
    protected JButton addCollabRowBtn;
    protected ArrayList<CollaborationListItem> collaborationList;

    protected int gridHeight = 0;
    protected final int DEFAULT_NUM_ROWS = 3;
    protected int numCollaborationRows;

    public DocumentationDialog(boolean canBeParent) {
        super(canBeParent);

        panel = new JPanel(new GridBagLayout());
        patternName = new JTextField();
        patternIntent = new JTextArea();
        patternIntent.setLineWrap(true);
        addCollabRowBtn = new JButton("Add Row");
        collaborationList = new ArrayList<>();

        init();
    }

    @Nullable
    @Override
    protected abstract JComponent createCenterPanel();

    protected void addDocumentationInvariableBody() {
        addElementToPanel(getLabel("Pattern Name"));
        addElementToPanel(patternName);
        addElementToPanel(getLabel("Intent"));
        addElementToPanel(patternIntent);
        addCollaborationHeaderToPanel();
        addCollaborationListToPanel();

        addCollabRowBtn.addActionListener(e -> {
            addCollaborationRowToPanel();
            panel.revalidate();
        });
    }

    protected void addCollaborationListToPanel() {
        for (int i = 0; i < numCollaborationRows; i++) {
            addCollaborationRowToPanel();
        }
    }

    protected void addElementToPanel(JComponent jComponent){
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 4;
        c.gridx = 0;
        c.gridy = gridHeight;
        c.insets = JBUI.insetsBottom(5);
        panel.add(jComponent, c);

        gridHeight++;
    }

    protected void addCollaborationHeaderToPanel(){
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

    protected void addCollaborationRowToPanel(){
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

    protected void removeCollaborationRoles(){
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

    protected void removeCollaborationRowObjects(JTextField className, JTextField role, JLabel jLabel, JButton jButton) {
        panel.remove(className);
        panel.remove(role);
        panel.remove(jLabel);
        panel.remove(jButton);
    }

    protected JBLabel getLabel(String text){
        JBLabel jLabel = new JBLabel(text);
        jLabel.setComponentStyle(UIUtil.ComponentStyle.SMALL);
        jLabel.setFontColor(UIUtil.FontColor.BRIGHTER);
        jLabel.setBorder(JBUI.Borders.empty(0,5,2,0));
        return jLabel;
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

    @NotNull
    protected PatternInstance generatePatternInstanceFromUserInput() {
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

        return new PatternInstance(name, intent, roleObjects, objectRoles);
    }
}
