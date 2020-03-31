package inspections;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.refactoring.listeners.RefactoringElementListenerProvider;
import org.jetbrains.annotations.Nullable;

public class ClassNameRefactoringListenerProvider implements RefactoringElementListenerProvider {

    @Nullable
    @Override
    public RefactoringElementListener getListener(PsiElement element) {
        if (element instanceof PsiClass) {
            PsiClass psiClass = (PsiClass) element;
            String oldName = psiClass.toString().split(":")[1];
            if (oldName != null) {
                return new ClassNameRefactoringListener(oldName);
            }
        }
        return null;
    }
}
