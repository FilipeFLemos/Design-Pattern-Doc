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
import ui.MyToolWindowFactory;

import static com.intellij.openapi.ui.DialogWrapper.NEXT_USER_EXIT_CODE;

public class EditPatternInstanceQuickFix implements LocalQuickFix {

    public static final String QUICK_FIX_NAME = "Edit pattern instance";
    private String object, patternInstanceId;
    private static EditDocumentationDialog editDocumentationDialog;

    public EditPatternInstanceQuickFix(String object, String patternInstanceId){
        this.object = object;
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
        final Application application = ApplicationManager.getApplication();
        application.invokeLater(() -> {
            editDocumentationDialog = new EditDocumentationDialog(object, patternInstanceId);
            MyToolWindowFactory.wasEditSuggestion = true;
            MyToolWindowFactory.updateWindow(editDocumentationDialog);
        });

    }
}
