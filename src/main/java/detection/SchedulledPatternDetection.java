package detection;

import models.PatternInstance;
import storage.PluginState;

import java.util.Set;

public class SchedulledPatternDetection implements Runnable{

    @Override
    public void run() {
        DetectionTool detectionTool = getDetectionTool("DPCORE");
        Set<PatternInstance> patternInstances = detectionTool.scanForPatterns();

        PatternSuggestions patternSuggestions = PluginState.getInstance().getPatternSuggestions();
        patternSuggestions.updateSuggestions(patternInstances);
    }

    public DetectionTool getDetectionTool(String toolName){
        return new DPCORE();
    }
}
