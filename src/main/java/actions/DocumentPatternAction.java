package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import ui.DocumentDialog;

public class DocumentPatternAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        if(new DocumentDialog(true).showAndGet()){
            //ok
        }
    }
}
