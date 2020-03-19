package detection;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import models.PatternInstance;
import storage.PluginState;
import java.util.*;

public class DetectPatternsAction extends AnAction {




    /**
     * Scans the project for design patterns using an implementation of a design pattern detection tool. By default, it
     * uses DP-CORE. Create a class that extends AbstractDetectionTool to use a different detection tool.
     * @param e - The action event
     */
    @Override
    public void actionPerformed(AnActionEvent e) {

        AbstractDetectionTool detectionTool = new DPCORE_DetectionTool();
        Set<PatternInstance> patternInstances = detectionTool.scanForPatterns();

        //PluginState pluginState = PluginState.getInstance();

//        for(PatternInstance patternInstance : patternInstances){
//            pluginState.addSuggestion(patternInstance);
//        }

        PatternSuggestions patternSuggestions = PluginState.getInstance().getPatternSuggestions();
        patternSuggestions.updateSuggestions(patternInstances);
        int i = 0;
    }
}
