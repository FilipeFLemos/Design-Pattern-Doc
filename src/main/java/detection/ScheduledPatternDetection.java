package detection;

import models.PatternInstance;
import storage.PluginState;
import storage.ProjectDetails;

import java.util.Set;

public class ScheduledPatternDetection implements Runnable{

    private Set<PatternInstance> patternInstances;
    private Set<String> allFileNamesFromProject;

    @Override
    public void run() {
        DetectionTool detectionTool = getDetectionTool("DPCORE");
        patternInstances = detectionTool.scanForPatterns();

        ProjectDetails projectDetails = PluginState.getInstance().getProjectDetails();
        allFileNamesFromProject = projectDetails.getAllFileNamesFromProject();

        if(wasRefactorHappeningAtSameTime()){
            return;
        }

        PatternSuggestions patternSuggestions = PluginState.getInstance().getPatternSuggestions();
        patternSuggestions.updateSuggestions(patternInstances);
    }

    public DetectionTool getDetectionTool(String toolName){
        return new DPCORE();
    }

    private boolean wasRefactorHappeningAtSameTime(){
        for(PatternInstance patternInstance : patternInstances){
            Set<String> detectedObjects = patternInstance.getObjectRoles().keySet();
            for(String detectedObject : detectedObjects){
                if(!allFileNamesFromProject.contains(detectedObject)){
                    return true;
                }
            }
        }
        return false;
    }
}
