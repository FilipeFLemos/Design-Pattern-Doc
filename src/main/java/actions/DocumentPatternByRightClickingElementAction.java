package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import ui.CreateDocumentationDialog;

public class DocumentPatternByRightClickingElementAction extends AnAction {

    private String className;

    @Override
    public void update(AnActionEvent e) {
        PsiElement psiElement = e.getData(LangDataKeys.PSI_ELEMENT);
        if(psiElement instanceof PsiClass){
            className = psiElement.toString().split(":")[1];
            e.getPresentation().setEnabledAndVisible(true);
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