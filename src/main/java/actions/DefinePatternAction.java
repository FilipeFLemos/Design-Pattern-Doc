package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.psi.PsiElement;
import ui.DefinePatternNameAndRolesDialog;
import ui.DefinePatternRolesLinksDialog;

import java.util.Set;

public class DefinePatternAction extends AnAction {

    @Override
    public void update(AnActionEvent e) {
        PsiElement psiElement = e.getData(LangDataKeys.PSI_ELEMENT);
        e.getPresentation().setEnabledAndVisible(psiElement == null);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        DefinePatternNameAndRolesDialog firstDialog = new DefinePatternNameAndRolesDialog(true);
        DefinePatternRolesLinksDialog secondDialog;
        firstDialog.show();

        if(firstDialog.getExitCode() == firstDialog.NEXT_STEP_DIALOG){
            String name = firstDialog.getName();
            Set<String> roles = firstDialog.getRoles();

            secondDialog = new DefinePatternRolesLinksDialog(true, name, roles);
            secondDialog.show();
        }
    }
}
