package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.source.PsiMethodImpl;
import ui.CreateDocumentationDialog;

public class DocumentPatternByRightClickingElementAction extends AnAction {

    private String className;

    @Override
    public void update(AnActionEvent e) {
        PsiElement psiElement = e.getData(LangDataKeys.PSI_ELEMENT);
        if (psiElement instanceof PsiClass) {
            className = psiElement.toString().split(":")[1];
            e.getPresentation().setEnabledAndVisible(true);
        }
        else if(psiElement instanceof PsiMethod){
            boolean visibility = ((PsiMethod) psiElement).getReturnType() == null;
            className = psiElement.toString().split(":")[1];
            e.getPresentation().setEnabledAndVisible(visibility);
        }
        else{
            e.getPresentation().setEnabledAndVisible(false);
        }
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        new CreateDocumentationDialog(true, className).showAndGet();
    }
}
