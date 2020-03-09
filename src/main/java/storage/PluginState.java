package storage;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.WindowManager;
import models.PatternInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.HashSet;


@State(
        name = "PatternInstances",
        storages = @Storage("pattern_instances.xml")
)
public class PluginState implements PersistentStateComponent<PersistentState> {

    private PersistentState persistentState = new PersistentState();
    private HashSet<PatternInstance> hints = new HashSet<>();
    private ProjectState projectState;
    private String projectName;

    public PluginState() {
        String activeProjectName = getActiveProjectName();
        setProjectName(activeProjectName);
    }

    public static PluginState getInstance() {
        return ServiceManager.getService(PluginState.class);
    }

    @Nullable
    @Override
    public PersistentState getState() {
        return persistentState;
    }

    @Override
    public void loadState(@NotNull PersistentState state) {
        persistentState = state;
    }

    public void addHint(PatternInstance patternInstance) {
        if (hints.contains(patternInstance)) {
            return;
        }

        boolean isAnHint = true;
        ProjectState projectState = getProjectState();

        if (projectState.hasAlreadyStored(patternInstance)) {
            isAnHint = false;
        }

        patternInstance.setAnHint(isAnHint);
        hints.add(patternInstance);
        //TODO: REMOVE AFTER DEBUGGING
        //projectState.storePatternInstanceIfAbsent("ok", patternInstance);
    }

    public HashSet<PatternInstance> getHints() {
        return hints;
    }

    public ProjectState getProjectState() {
        String activeProjectName = getActiveProjectName();
        if (!existsProjectState() || !projectName.equals(activeProjectName)) {
            setActiveProject();
        }
        return projectState;
    }

    private boolean existsProjectState() {
        return projectState != null;
    }

    private void setActiveProject() {
        String activeProjectName = getActiveProjectName();
        setProjectName(activeProjectName);
        setProjectState();
    }

    private void setProjectState() {
        if (!persistentState.containsProject(projectName)) {
            persistentState.putProjectState(projectName);
        }
        projectState = persistentState.getProjectState(projectName);
    }

    private String getActiveProjectName() {
        String activeProjectName = "";
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        for (Project project : projects) {
            Window window = WindowManager.getInstance().suggestParentWindow(project);
            if (window != null && window.isActive()) {
                activeProjectName = project.getName();
                break;
            }
        }
        return activeProjectName;
    }

    private void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}
