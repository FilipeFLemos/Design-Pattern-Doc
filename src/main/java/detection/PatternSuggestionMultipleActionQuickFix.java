package detection;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import models.PatternInstance;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class PatternSuggestionMultipleActionQuickFix implements LocalQuickFix {

    private String QUICK_FIX_NAME, object;
    private Set<PatternInstance> patternInstances;
    private boolean isIgnore;

    public PatternSuggestionMultipleActionQuickFix(Set<PatternInstance> patternInstances, String object, boolean isIgnore) {
        this.patternInstances = patternInstances;
        this.object = object;
        this.isIgnore = isIgnore;

        String quickFixName;
        if(isIgnore){
            quickFixName = "Ignore";
        }
        else{
            quickFixName = "Add pattern instance documentation";
        }
        this.QUICK_FIX_NAME = quickFixName;
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getFamilyName() {
        return QUICK_FIX_NAME;
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        final Application application = ApplicationManager.getApplication();
        application.invokeLater(() -> {
            PatternSuggestionsActionDialog patternSuggestionsActionDialog = new PatternSuggestionsActionDialog(true, isIgnore, patternInstances, object);
            patternSuggestionsActionDialog.show();
        });
    }
}
