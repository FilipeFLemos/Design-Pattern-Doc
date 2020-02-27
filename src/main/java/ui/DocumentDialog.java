package ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBLabel;
import com.intellij.uiDesigner.core.AbstractLayout;
import com.intellij.util.ui.GridBag;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class DocumentDialog extends DialogWrapper {

    private JPanel panel;
    private JTextField patternName;
    private JTextArea patternIntent;
    private JTextArea patternCollaborations;

    public DocumentDialog(boolean canBeParent) {
        super(canBeParent);

        panel = new JPanel(new GridBagLayout());
        patternName = new JTextField();
        patternIntent = new JTextArea();
        patternIntent.setLineWrap(true);
        patternCollaborations = new JTextArea();
        patternCollaborations.setLineWrap(true);

        init();
        setTitle("Document Pattern Instance");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {

        GridBag gridBag = new GridBag();
        gridBag.setDefaultInsets(JBUI.insets(0, 0, AbstractLayout.DEFAULT_VGAP, AbstractLayout.DEFAULT_HGAP));
        gridBag.setDefaultWeightX(1.0);
        gridBag.setDefaultFill(GridBagConstraints.HORIZONTAL);

        panel.setPreferredSize(new Dimension(400,200));

        panel.add(getLabel("Pattern Name"), gridBag.nextLine().next().weightx(0.2));
        panel.add(patternName, gridBag.nextLine().next().weightx(0.2));
        panel.add(getLabel("Intent"), gridBag.nextLine().next().weightx(0.2));
        panel.add(patternIntent, gridBag.nextLine().next().weightx(0.2));
        panel.add(getLabel("Collaborations"), gridBag.nextLine().next().weightx(0.2));
        panel.add(patternCollaborations, gridBag.nextLine().next().weightx(0.2));

        return panel;
    }

    @Override
    protected void doOKAction() {
        String name = patternName.getText();
        String intent = patternIntent.getText();
        String collaborations = patternCollaborations.getText();
        System.out.println("");
        close(OK_EXIT_CODE);
    }

    private JBLabel getLabel(String text){
        JBLabel jLabel = new JBLabel(text);
        jLabel.setComponentStyle(UIUtil.ComponentStyle.SMALL);
        jLabel.setFontColor(UIUtil.FontColor.BRIGHTER);
        jLabel.setBorder(JBUI.Borders.empty(0,5,2,0));
        return jLabel;
    }
}
