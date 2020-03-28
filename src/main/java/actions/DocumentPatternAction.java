package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.psi.PsiElement;
import ui.CreateDocumentationDialog;

public class DocumentPatternAction extends AnAction {

    @Override
    public void update(AnActionEvent e) {
        PsiElement psiElement = e.getData(LangDataKeys.PSI_ELEMENT);
        e.getPresentation().setEnabledAndVisible(psiElement == null);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        new CreateDocumentationDialog(true).showAndGet();
    }
}
