package detection;

import models.PatternInstance;
import storage.PluginState;
import storage.ProjectDetails;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

public class PatternDetection implements Runnable {

    private Set<PatternInstance> patternInstances;
    private Set<String> allFileNamesFromProject;

    @Override
    public void run() {
        ProjectDetails projectDetails = PluginState.getInstance().getProjectDetails();

        DetectionTool detectionTool = new DPCORE();
        patternInstances = detectionTool.scanForPatterns();

        allFileNamesFromProject = projectDetails.getAllFileNamesFromProject();

        if (wasRefactorHappeningAtSameTime()) {
            return;
        }

        PatternSuggestions patternSuggestions = PluginState.getInstance().getPatternSuggestions();
        patternSuggestions.updateSuggestions(patternInstances);
    }

    private boolean wasRefactorHappeningAtSameTime() {
        for (PatternInstance patternInstance : patternInstances) {
            Set<String> detectedObjects = patternInstance.getObjectRoles().keySet();
            for (String detectedObject : detectedObjects) {
                if (!allFileNamesFromProject.contains(detectedObject)) {
                    return true;
                }
            }
        }
        return false;
    }
}
