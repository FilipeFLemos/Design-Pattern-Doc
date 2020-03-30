package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.psi.PsiElement;
import ui.DefinePatternDialog;

public class DefinePatternAction extends AnAction{

    @Override
    public void update(AnActionEvent e) {
        PsiElement psiElement = e.getData(LangDataKeys.PSI_ELEMENT);
        e.getPresentation().setEnabledAndVisible(psiElement == null);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        new DefinePatternDialog(true).showAndGet();
    }
}
