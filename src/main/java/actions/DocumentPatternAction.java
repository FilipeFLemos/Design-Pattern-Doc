package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import ui.CreateDocumentationDialog;

public class DocumentPatternAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        new CreateDocumentationDialog(true).showAndGet();
    }
}
