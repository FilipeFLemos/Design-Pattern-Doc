package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import ui.DefinePatternDialog;

public class DefinePatternAction extends AnAction{
    @Override
    public void actionPerformed(AnActionEvent e) {
        new DefinePatternDialog(true).showAndGet();
    }
}
