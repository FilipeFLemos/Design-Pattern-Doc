package storage;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.QuickFix;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.psi.PsiElement;
import com.intellij.util.concurrency.AppExecutorUtil;
import detection.PatternSuggestionQuickFix;
import detection.PatternSuggestions;
import detection.SchedulledPatternDetection;
import models.PatternInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.Utils;

import java.util.ArrayList;
import java.util.Map;
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
    private ProblemsHolder problemsHolder;

    public PluginState() {
        projectDetails = new ProjectDetails();
        patternSuggestions = new PatternSuggestions();
        AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay(new SchedulledPatternDetection(), 0, Utils.PATTERN_DETECTION_DELAY, TimeUnit.SECONDS);
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

    public PatternSuggestions getPatternSuggestions() {
        return patternSuggestions;
    }

    public ProjectDetails getProjectDetails() {
        return projectDetails;
    }

    public void setHolder(ProblemsHolder holder) {
        this.problemsHolder = holder;
    }

    public ProblemsHolder getProblemsHolder() {
        return problemsHolder;
    }
}
