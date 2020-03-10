package inspections;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import models.PatternInstance;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import storage.PluginState;

public class PatternSuggestionQuickFix implements LocalQuickFix {

    public static final String QUICK_FIX_NAME = "Add Pattern Instance Documentation";
    private PatternInstance patternInstance;
    private ProblemsHolder problemsHolder;

    public PatternSuggestionQuickFix(PatternInstance patternInstance, ProblemsHolder problemsHolder){
        this.patternInstance = patternInstance;
        this.problemsHolder = problemsHolder;
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getFamilyName() {
        return QUICK_FIX_NAME;
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        PluginState.getInstance().updateStorage(patternInstance);

        PsiElement element = descriptor.getPsiElement();
        PsiElement replacedElement = element.copy();

        PsiElement nextSibling = element.getNextSibling();
        PsiElement father = element.getParent();
        father.addBefore(replacedElement, nextSibling);
        element.delete();
    }
}
