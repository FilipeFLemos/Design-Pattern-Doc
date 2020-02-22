package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.DialogWrapper;
import ui.DocumentDialog;

public class DocumentPatternAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        if(new DocumentDialog(true).showAndGet()){
            //ok
        }
    }
}
