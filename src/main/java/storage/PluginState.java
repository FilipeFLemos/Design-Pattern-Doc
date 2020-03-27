package storage;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.concurrency.AppExecutorUtil;
import detection.PatternSuggestions;
import detection.ScheduledPatternDetection;
import models.DesignPattern;
import models.PatternInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.Utils;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@State(
        name = "PatternInstances",
        storages = @Storage("pattern_instances.xml")
)
public class PluginState implements PersistentStateComponent<ProjectsPersistedState> {

    private ProjectsPersistedState persistentState = new ProjectsPersistedState();
    private ProjectDetails projectDetails;
    private PatternSuggestions patternSuggestions;

    public PluginState() {
        projectDetails = new ProjectDetails();
        patternSuggestions = new PatternSuggestions();
        AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay(new ScheduledPatternDetection(), 0, Utils.PATTERN_DETECTION_DELAY, TimeUnit.SECONDS);
    }

    public static PluginState getInstance() {
        return ServiceManager.getService(PluginState.class);
    }

    @Nullable
    @Override
    public ProjectsPersistedState getState() {
        return persistentState;
    }

    @Override
    public void loadState(@NotNull ProjectsPersistedState state) {
        persistentState = state;
    }

    public void updateStorage(PatternInstance patternInstance){
        projectDetails.updateProjectPersistedState(patternInstance);
    }

    public Set<DesignPattern> getSupportedDesignPatterns(){
        Set<DesignPattern> designPatterns = new HashSet<>();
        ProjectsPersistedState projectsPersistedState = getState();
        if(projectsPersistedState == null){
            return designPatterns;
        }

        if(projectsPersistedState.areSupportDesignPatternsNotInitialized()){
            projectsPersistedState.setSupportedDesignPatterns(Utils.getSupportedDesignPatterns());
        }

        return projectsPersistedState.getSupportedDesignPatterns();
    }

    public PatternSuggestions getPatternSuggestions() {
        return patternSuggestions;
    }

    public ProjectDetails getProjectDetails() {
        return projectDetails;
    }

    public void restartHighlighting(){
        Project project = projectDetails.getActiveProject();
        final DaemonCodeAnalyzer analyzer = DaemonCodeAnalyzer.getInstance(project);
        analyzer.restart();
    }
}
