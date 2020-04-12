package storage;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.concurrency.AppExecutorUtil;
import detection.PatternSuggestions;
import detection.PatternDetectionScheduler;
import models.DesignPattern;
import models.PatternInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.PlantUmlHelper;
import utils.Utils;

import java.io.*;
import java.util.ArrayList;
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
    private ArrayList<File> patternDescriptions;

    public PluginState() {
        projectDetails = new ProjectDetails();
        patternSuggestions = new PatternSuggestions();
        setPatternDescriptions();
        sendNotificationIfGraphvizNotInstalled();
        AppExecutorUtil.getAppScheduledExecutorService().schedule(new PatternDetectionScheduler(), 10, TimeUnit.SECONDS);
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

    public void updateStorage(PatternInstance patternInstance) {
        projectDetails.updateProjectPersistedState(patternInstance);
    }

    public Set<DesignPattern> getSupportedDesignPatterns() {
        Set<DesignPattern> designPatterns = new HashSet<>();
        ProjectsPersistedState projectsPersistedState = getState();
        if (projectsPersistedState == null) {
            return designPatterns;
        }

        if (projectsPersistedState.areSupportDesignPatternsNotInitialized()) {
            projectsPersistedState.setSupportedDesignPatterns(Utils.getSupportedDesignPatterns());
        }

        return projectsPersistedState.getSupportedDesignPatterns();
    }

    public void restartHighlighting() {
        Project project = projectDetails.getActiveProject();
        final DaemonCodeAnalyzer analyzer = DaemonCodeAnalyzer.getInstance(project);
        analyzer.restart();
    }

    public PatternSuggestions getPatternSuggestions() {
        return patternSuggestions;
    }

    public ProjectDetails getProjectDetails() {
        return projectDetails;
    }

    public ArrayList<File> getPatternDescriptions() {
        return patternDescriptions;
    }

    private void setPatternDescriptions() {
        ArrayList<File> patternDescriptions = new ArrayList<>();
        ArrayList<String> patternFileNames = Utils.getPatternFileNames();

        for (String patternFileName : patternFileNames) {
            File file;
            String resource = "patterns/" + patternFileName;

            try {
                String[] fileNameParsed = patternFileName.split(".pattern");
                InputStream input = getClass().getClassLoader().getResourceAsStream(resource);
                file = File.createTempFile(fileNameParsed[0], ".pattern");
                OutputStream out = new FileOutputStream(file);
                int read;
                byte[] bytes = new byte[1024];

                while ((read = input != null ? input.read(bytes) : 0) != -1) {
                    out.write(bytes, 0, read);
                }
                out.close();
                file.deleteOnExit();

                patternDescriptions.add(file);

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        this.patternDescriptions = patternDescriptions;
    }

    private void sendNotificationIfGraphvizNotInstalled() {
        if(!PlantUmlHelper.isGraphvizInstalled()){
            Notifications.Bus.notify(new Notification("Design Pattern Doc", "Design Pattern Doc: Please Install Graphviz", "Graphviz must be installed to fully experience the plugin!", NotificationType.WARNING));
        }
    }
}
