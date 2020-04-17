package inspections;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import ui.EditDocumentationDialog;

import static com.intellij.openapi.ui.DialogWrapper.NEXT_USER_EXIT_CODE;

public class EditPatternInstanceQuickFix implements LocalQuickFix {

    public static final String QUICK_FIX_NAME = "Edit pattern instance";
    private String patternInstanceId;
    private static EditDocumentationDialog editDocumentationDialog;

    public EditPatternInstanceQuickFix(String patternInstanceId){
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
        editDocumentationDialog = new EditDocumentationDialog(true, patternInstanceId);
        showDialog(project);
    }

    public void showDialog(Project project){
        final Application application = ApplicationManager.getApplication();
        application.invokeLater(() -> {
            editDocumentationDialog.show();
            checkDialogCloseAction(project);
        });
    }

    private void checkDialogCloseAction(Project project) {
        if (isPatternInstanceDocDeleted()) {
            Messages.showMessageDialog(project, "The selected pattern instance documentation was deleted from the persistent storage!", "Info", Messages.getInformationIcon());
        }
    }

    private boolean isPatternInstanceDocDeleted() {
        return editDocumentationDialog.getExitCode() == NEXT_USER_EXIT_CODE;
    }
}
