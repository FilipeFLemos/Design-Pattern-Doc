package inspections;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.psi.*;
import models.PatternInstance;
import storage.PluginState;
import storage.ProjectDetails;
import storage.ProjectPersistedState;
import ui.EditDocumentationDialog;
import ui.MyToolWindowFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CursorInspection implements Runnable {

    private PsiElement psiElement;
    private String className;

    @Override
    public void run() {
        while (true) {
            cursorInspect();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private void cursorInspect(){
        final Application application = ApplicationManager.getApplication();
        application.invokeLater(() -> {
            PsiElement elementFocused = getFocusedElement();
            if(elementFocused == null || (elementFocused.equals(psiElement) && !MyToolWindowFactory.isReset)){
                return;
            }

            psiElement = elementFocused;

            if(MyToolWindowFactory.wasEditSuggestion){
                MyToolWindowFactory.wasEditSuggestion = false;
                return;
            }

            if ((psiElement instanceof PsiClass || psiElement instanceof PsiIdentifier) && existsPatternInstanceForObject(psiElement)){
                updateToolWindow();
            }
            else if(psiElement instanceof PsiMethod && ((PsiMethod) psiElement).getReturnType() == null && existsPatternInstanceForObject(psiElement)){
                updateToolWindow();
            }
            else{
                MyToolWindowFactory.clearPanel();
            }
        });
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

    protected ConcurrentHashMap<String, PatternInstance> getStringPatternInstanceConcurrentHashMap() {
        ProjectDetails projectDetails = PluginState.getInstance().getProjectDetails();
        ProjectPersistedState projectPersistedState = projectDetails.getActiveProjectPersistedState();
        return projectPersistedState.getPatternInstanceById();
    }

    private String getClassName(PsiElement psiElement) {
        return psiElement.toString().split(":")[1];
    }

    protected PsiElement getFocusedElement(){
        Project project = PluginState.getInstance().getProjectDetails().getActiveProject();
        if(project == null || project.isDisposed()){
            return null;
        }
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        VirtualFile file = FileDocumentManager.getInstance().getFile(editor.getDocument());
        int offset = editor.getCaretModel().getOffset();

        return PsiManager.getInstance(project).findFile(file).findElementAt(offset);
    }

    private void updateToolWindow(){
        EditDocumentationDialog editDocumentationDialog = new EditDocumentationDialog(true, className);
        MyToolWindowFactory.updateWindow(editDocumentationDialog);
    }
}
