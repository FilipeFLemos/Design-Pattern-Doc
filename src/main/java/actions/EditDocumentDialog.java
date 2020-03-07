package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import models.PatternInstance;
import storage.PersistentState;
import storage.PluginState;
import ui.DocumentDialog;

import java.util.concurrent.ConcurrentHashMap;

import static com.intellij.openapi.ui.DialogWrapper.NEXT_USER_EXIT_CODE;

public class EditDocumentDialog extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {

        PersistentState persistentState = (PersistentState) PluginState.getInstance().getState();
        ConcurrentHashMap<String, PatternInstance> patternInstanceById = persistentState.getPatternInstanceById();

        if(patternInstanceById == null || patternInstanceById.isEmpty()){
            Messages.showMessageDialog(e.getProject(), "You need at least one pattern instance stored to use this feature!", "Info", Messages.getInformationIcon());
            return;
        }

        DocumentDialog documentDialog = new DocumentDialog(true, true);
        documentDialog.show();
        if(documentDialog.getExitCode() == NEXT_USER_EXIT_CODE){
            Messages.showMessageDialog(e.getProject(), "The selected pattern instance documentation was deleted from the persistent storage!", "Info", Messages.getInformationIcon());
        }
    }
}
