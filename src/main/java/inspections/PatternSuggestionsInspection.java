package inspections;


import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

public class PatternSuggestionsInspection extends AbstractBaseJavaLocalInspectionTool {

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new JavaElementVisitor() {

            private final String DESCRIPTION_TEMPLATE = "SDK: This class plays the Role X in Design Pattern Y";

            @Override
            public void visitElement(PsiElement element) {
                if(element.textMatches("PluginState")){
                    holder.registerProblem(element,DESCRIPTION_TEMPLATE);

                }
            }
        };
    }

}
