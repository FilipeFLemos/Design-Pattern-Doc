package ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public class MyToolWindowFactory implements ToolWindowFactory {

    private static ToolWindow toolWindow;
    public static boolean isReset, wasEditSuggestion;

    public static void resetPanel() {
        if(toolWindow == null){
            return;
        }
        toolWindow.getContentManager().removeAllContents(true);
        isReset = true;
    }

    public static void clearPanel() {
        if(toolWindow == null){
            return;
        }
        toolWindow.getContentManager().removeAllContents(true);
    }

    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        MyToolWindowFactory.toolWindow = toolWindow;
        clearPanel();
        isReset = false;
        wasEditSuggestion = false;
    }

    public static void updateWindow(DocumentationDialog documentationDialog){
        if(toolWindow == null){
            return;
        }
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(documentationDialog.getPanel(), "", false);
        toolWindow.getContentManager().removeAllContents(true);
        toolWindow.getContentManager().addContent(content);
        MyToolWindowFactory.isReset = false;
    }
}