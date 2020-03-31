package actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.psi.PsiElement;
import models.PatternInstance;
import ui.EditDocumentationDialog;

import java.util.concurrent.ConcurrentHashMap;

public class EditDocumentationAction extends EditDocumentation {

    @Override
    public void update(AnActionEvent e) {
        PsiElement psiElement = e.getData(LangDataKeys.PSI_ELEMENT);
        if (psiElement == null && existsPatternInstanceStored()) {
            e.getPresentation().setEnabledAndVisible(true);
        } else {
            e.getPresentation().setEnabledAndVisible(false);
        }
    }

    private boolean existsPatternInstanceStored() {
        ConcurrentHashMap<String, PatternInstance> patternInstanceById = getStringPatternInstanceConcurrentHashMap();
        return patternInstanceById != null && !patternInstanceById.isEmpty();
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        displayEditDocumentationDialog();
        checkDialogCloseAction(e);
    }

    private void displayEditDocumentationDialog() {
        editDocumentationDialog = new EditDocumentationDialog(true);
        editDocumentationDialog.show();
    }
}
