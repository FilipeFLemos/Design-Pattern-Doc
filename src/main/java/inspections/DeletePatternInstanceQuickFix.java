package inspections;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import storage.PluginState;
import storage.ProjectDetails;
import storage.ProjectPersistedState;

public class DeletePatternInstanceQuickFix implements LocalQuickFix {

    public static final String QUICK_FIX_NAME = "Remove pattern instance";
    private String patternInstanceId;
    private ProjectPersistedState projectPersistedState;

    public DeletePatternInstanceQuickFix(String patternInstanceId){
        this.patternInstanceId = patternInstanceId;
        ProjectDetails projectDetails = PluginState.getInstance().getProjectDetails();
        projectPersistedState = projectDetails.getActiveProjectPersistedState();
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getFamilyName() {
        return QUICK_FIX_NAME;
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        projectPersistedState.deletePatternInstance(patternInstanceId);
    }
}
