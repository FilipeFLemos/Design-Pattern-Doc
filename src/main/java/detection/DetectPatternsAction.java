package detection;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import models.PatternInstance;
import storage.PluginState;
import java.util.*;

public class DetectPatternsAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {

        DetectionTool detectionTool = getDetectionTool("DPCORE");
        Set<PatternInstance> patternInstances = detectionTool.scanForPatterns();

        PatternSuggestions patternSuggestions = PluginState.getInstance().getPatternSuggestions();
        patternSuggestions.updateSuggestions(patternInstances);
    }

    public DetectionTool getDetectionTool(String toolName){
        return new DPCORE();
    }
}
