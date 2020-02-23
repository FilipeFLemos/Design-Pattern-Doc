package models;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@State(
        name = "PatternInstances",
        storages = @Storage("pattern_instances.xml")
)
public class PersistentStateManager implements PersistentStateComponent<PersistentState> {

    private PersistentState persistentState = new PersistentState();

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
        return ServiceManager.getService(PersistentStateManager.class);
    }
}
