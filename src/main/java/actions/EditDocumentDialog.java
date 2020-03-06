package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import ui.DocumentDialog;

public class EditDocumentDialog extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        if(new DocumentDialog(true, true).showAndGet()){
            //ok
        }
    }
}
