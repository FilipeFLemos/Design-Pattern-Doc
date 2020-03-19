package storage;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import detection.PatternSuggestions;
import models.PatternInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.Utils;

import java.util.HashSet;


@State(
        name = "PatternInstances",
        storages = @Storage("pattern_instances.xml")
)
public class PluginState implements PersistentStateComponent<ProjectsPersistedState> {

    private ProjectsPersistedState persistentState = new ProjectsPersistedState();
    private HashSet<PatternInstance> suggestions = new HashSet<>();
    private ProjectDetails projectDetails;
    private PatternSuggestions patternSuggestions;

    public PluginState() {
        projectDetails = new ProjectDetails();
        patternSuggestions = new PatternSuggestions();
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
//        if (suggestions.contains(patternInstance)) {
//            patternInstance.setAnHint(false);
//            suggestions.remove(patternInstance);
//            suggestions.add(patternInstance);
//        }
    }

    public ProjectPersistedState getProjectPersistedState() {
       return projectDetails.getActiveProjectPersistedState();
    }

    public String getProjectPath(){
        return projectDetails.getPath();
    }

    public PatternSuggestions getPatternSuggestions() {
        return patternSuggestions;
    }
}
