package detection;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import models.PatternInstance;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import storage.PluginState;

public class PatternSuggestionQuickFix implements LocalQuickFix {

    public static final String QUICK_FIX_NAME = "Add pattern instance documentation";
    private PatternInstance patternInstance;
    private PatternSuggestions patternSuggestions;

    public PatternSuggestionQuickFix(PatternInstance patternInstance) {
        this.patternInstance = patternInstance;
        PluginState pluginState = PluginState.getInstance();
        patternSuggestions = pluginState.getPatternSuggestions();
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getFamilyName() {
        return QUICK_FIX_NAME;
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        patternSuggestions.acceptAvailableSuggestion(patternInstance);
        PatternInstance copyPatternInstance = new PatternInstance(patternInstance);
        PluginState.getInstance().updateStorage(copyPatternInstance);

    }
}
