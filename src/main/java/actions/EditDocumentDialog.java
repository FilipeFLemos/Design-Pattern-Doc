package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import models.PatternInstance;
import storage.PersistentState;
import storage.PluginState;
import ui.DocumentDialog;

import java.util.concurrent.ConcurrentHashMap;

public class EditDocumentDialog extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {

        PersistentState persistentState = (PersistentState) PluginState.getInstance().getState();
        ConcurrentHashMap<String, PatternInstance> patternInstanceById = persistentState.getPatternInstanceById();

        if(patternInstanceById == null || patternInstanceById.isEmpty()){
            Messages.showMessageDialog(e.getProject(), "You need at least one pattern instance stored to use this feature!", "Warning", Messages.getInformationIcon());
            return;
        }

        if(new DocumentDialog(true, true).showAndGet()){
            //ok
        }
    }
}
