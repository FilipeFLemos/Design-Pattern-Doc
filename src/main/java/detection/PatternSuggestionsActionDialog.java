package detection;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import models.PatternInstance;
import org.jetbrains.annotations.Nullable;
import storage.PluginState;
import storage.ProjectDetails;
import utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PatternSuggestionsActionDialog  extends DialogWrapper {

    private boolean isIgnore;
    private JPanel panel;
    private ComboBox patternInstanceComboBox;
    private Map<String, PatternInstance> patternInstancesMap;
    private String object;

    protected PatternSuggestionsActionDialog(boolean canBeParent, boolean isIgnore, Set<PatternInstance> patternInstances, String object) {
        super(canBeParent);
        this.isIgnore = isIgnore;
        this.object = object;

        panel = new JPanel(new FlowLayout());

        patternInstancesMap = new HashMap<>();
        patternInstanceComboBox = getPatternInstanceComboBox(patternInstances);
        patternInstanceComboBox.setMinimumAndPreferredWidth(300);

        String title;
        if(isIgnore){
            title = "Which suggestion would you like to ignore?";
        }
        else{
            title = "Which suggestion would you accept?";
        }
        setTitle(title);
        setResizable(false);
        init();
    }

    private ComboBox getPatternInstanceComboBox(Set<PatternInstance> patternInstances) {
        String[] patterns = new String[patternInstances.size()];
        int index = 0;
        for (PatternInstance patternInstance : patternInstances) {
            String objectRolesText = Utils.getObjectRolesText(patternInstance, object);
            patterns[index] = patternInstance.getPatternName() + ":" + objectRolesText;
            patternInstancesMap.put(patterns[index], patternInstance);
            index++;
        }
        return new ComboBox(patterns);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        panel.add(patternInstanceComboBox);
        return panel;
    }

    @Override
    protected void doOKAction() {
        String selectedOption = (String) patternInstanceComboBox.getSelectedItem();
        PatternInstance selectedPatternInstance = patternInstancesMap.get(selectedOption);

        PluginState pluginState = PluginState.getInstance();
        PatternSuggestions patternSuggestions = pluginState.getPatternSuggestions();
        ProjectDetails projectDetails = pluginState.getProjectDetails();

        patternSuggestions.acceptAvailableSuggestion(selectedPatternInstance);

        if(!isIgnore){
            PatternInstance copyPatternInstance = new PatternInstance(selectedPatternInstance);
            PluginState.getInstance().updateStorage(copyPatternInstance);
            projectDetails.updateUmlFileByPatternInstance(selectedPatternInstance);
        }

        close(OK_EXIT_CODE);
    }
}
