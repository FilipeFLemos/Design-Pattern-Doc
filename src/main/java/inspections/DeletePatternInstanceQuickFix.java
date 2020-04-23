package inspections;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import storage.PluginState;
import storage.ProjectPersistedState;
import storage.ProjectsPersistedState;

public class DeletePatternInstanceQuickFix implements LocalQuickFix {

    public static final String QUICK_FIX_NAME = "Remove pattern instance";
    private String patternInstanceId;

    public DeletePatternInstanceQuickFix(String patternInstanceId){
        this.patternInstanceId = patternInstanceId;
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getFamilyName() {
        return QUICK_FIX_NAME;
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        ProjectsPersistedState projectsPersistedState = PluginState.getInstance().getState();

        if (projectsPersistedState != null) {
            ProjectPersistedState projectPersistedState = projectsPersistedState.getProjectState(project.getName());
            projectPersistedState.deletePatternInstance(patternInstanceId);
        }
    }
}
