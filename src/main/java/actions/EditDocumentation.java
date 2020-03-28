package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import models.PatternInstance;
import storage.PluginState;
import storage.ProjectDetails;
import storage.ProjectPersistedState;
import ui.EditDocumentationDialog;

import java.util.concurrent.ConcurrentHashMap;

import static com.intellij.openapi.ui.DialogWrapper.NEXT_USER_EXIT_CODE;

public abstract class EditDocumentation extends AnAction {

    protected EditDocumentationDialog editDocumentationDialog;

    @Override
    public abstract void update(AnActionEvent e);

    @Override
    public abstract void actionPerformed(AnActionEvent e);

    protected ConcurrentHashMap<String, PatternInstance> getStringPatternInstanceConcurrentHashMap() {
        ProjectDetails projectDetails = PluginState.getInstance().getProjectDetails();
        ProjectPersistedState projectPersistedState = projectDetails.getActiveProjectPersistedState();
        return projectPersistedState.getPatternInstanceById();
    }

    protected void checkDialogCloseAction(AnActionEvent e){
        if(isPatternInstanceDocDeleted()){
            Messages.showMessageDialog(e.getProject(), "The selected pattern instance documentation was deleted from the persistent storage!", "Info", Messages.getInformationIcon());
        }
    }

    private boolean isPatternInstanceDocDeleted() {
        return editDocumentationDialog.getExitCode() == NEXT_USER_EXIT_CODE;
    }
}
