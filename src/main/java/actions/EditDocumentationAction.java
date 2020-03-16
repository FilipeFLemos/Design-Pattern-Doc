package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import models.PatternInstance;
import storage.PluginState;
import storage.ProjectPersistedState;
import ui.EditDocumentationDialog;

import java.util.concurrent.ConcurrentHashMap;

import static com.intellij.openapi.ui.DialogWrapper.NEXT_USER_EXIT_CODE;

public class EditDocumentationAction extends AnAction {

    EditDocumentationDialog editDocumentationDialog;

    @Override
    public void actionPerformed(AnActionEvent e) {

        try{
            checkDocumentationStorage();
            displayEditDocumentationDialog(e);
            checkDialogCloseAction(e);
        }catch(Exception exception){
            Messages.showMessageDialog(e.getProject(), "You need at least one pattern instance stored to use this feature!", "Info", Messages.getInformationIcon());
        }
    }

    private void checkDocumentationStorage() throws NullPointerException {
        ProjectPersistedState projectPersistedState = ((PluginState) PluginState.getInstance()).getProjectPersistedState();
        ConcurrentHashMap<String, PatternInstance> patternInstanceById = projectPersistedState.getPatternInstanceById();
        if(patternInstanceById == null || patternInstanceById.isEmpty()){
            throw new NullPointerException();
        }
    }

    private void displayEditDocumentationDialog(AnActionEvent e){
        editDocumentationDialog = new EditDocumentationDialog(true);
        editDocumentationDialog.show();
    }

    private void checkDialogCloseAction(AnActionEvent e){
        if(isPatternInstanceDocDeleted()){
            Messages.showMessageDialog(e.getProject(), "The selected pattern instance documentation was deleted from the persistent storage!", "Info", Messages.getInformationIcon());
        }
    }

    private boolean isPatternInstanceDocDeleted() {
        return editDocumentationDialog.getExitCode() == NEXT_USER_EXIT_CODE;
    }
}
