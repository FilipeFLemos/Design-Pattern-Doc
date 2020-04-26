package actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import models.PatternInstance;
import ui.EditDocumentationDialog;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class EditDocumentationByRightClickingElementAction extends EditDocumentation {

    private String className;

    @Override
    public void update(AnActionEvent e) {
        PsiElement psiElement = e.getData(LangDataKeys.PSI_ELEMENT);
        if (psiElement instanceof PsiClass && existsPatternInstanceForObject(psiElement)){
            e.getPresentation().setEnabledAndVisible(true);
        }
        else if(psiElement instanceof PsiMethod && ((PsiMethod) psiElement).getReturnType() == null && existsPatternInstanceForObject(psiElement)){
            e.getPresentation().setEnabledAndVisible(true);
        }
        else {
            e.getPresentation().setEnabledAndVisible(false);
        }
    }

    private boolean existsPatternInstanceForObject(PsiElement psiElement) {
        ConcurrentHashMap<String, PatternInstance> patternInstanceById = getStringPatternInstanceConcurrentHashMap();
        className = getClassName(psiElement);

        for (Map.Entry<String, PatternInstance> entry : patternInstanceById.entrySet()) {
            PatternInstance patternInstance = entry.getValue();
            Set<String> objects = patternInstance.getObjectRoles().keySet();
            if (objects.contains(className)) {
                return true;
            }
        }
        return false;
    }

    private String getClassName(PsiElement psiElement) {
        return psiElement.toString().split(":")[1];
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        editDocumentationDialog = new EditDocumentationDialog(true, className);
        editDocumentationDialog.show();
        checkDialogCloseAction(e);
    }
}
