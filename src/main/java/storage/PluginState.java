package storage;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import models.PatternInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;


@State(
        name = "PatternInstances",
        storages = @Storage("pattern_instances.xml")
)
public class PluginState implements PersistentStateComponent<PersistentState> {

    private PersistentState persistentState = new PersistentState();
    private HashSet<PatternInstance> hints = new HashSet<>();

    @Nullable
    @Override
    public PersistentState getState() {
        return persistentState;
    }

    @Override
    public void loadState(@NotNull PersistentState state) {
        persistentState = state;
    }

    public static PersistentStateComponent getInstance(){
        return ServiceManager.getService(PluginState.class);
    }

    public void addHint(PatternInstance patternInstance){
        if(hints.contains(patternInstance)){
            return;
        }

        boolean isAnHint = true;
        if(persistentState.hasAlreadyStored(patternInstance)){
            isAnHint = false;
        }

        patternInstance.setAnHint(isAnHint);
        hints.add(patternInstance);
    }

    public HashSet<PatternInstance> getHints() {
        return hints;
    }
}
