package utils;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorLinePainter;
import com.intellij.openapi.editor.LineExtensionInfo;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.JBColor;
import models.PatternInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import storage.PluginState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PatternHints extends EditorLinePainter {
    @Nullable
    @Override
    public Collection<LineExtensionInfo> getLineExtensions(@NotNull Project project, @NotNull VirtualFile file, int lineNumber) {

        Collection<LineExtensionInfo> collection = new ArrayList<>();
        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        if(psiFile == null){
            return collection;
        }

        Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
        int offsetStart = document.getLineStartOffset(lineNumber);
        int offsetEnd = document.getLineEndOffset(lineNumber);

        for(int i = offsetStart; i <= offsetEnd; i++){
            PsiElement psiElement = psiFile.findElementAt(i);
            if(psiElement == null)
                break;

            PluginState pluginState = (PluginState) PluginState.getInstance();
            ConcurrentHashMap<String, PatternInstance> hints = pluginState.getState().getPatternInstanceById();
            if(hints == null)
                break;

            for (Map.Entry<String, PatternInstance> entry : hints.entrySet()) {
                PatternInstance patternInstance = entry.getValue();
                String patternName = patternInstance.getPatternName();

                for (Map.Entry<String, Set<String>> entry2 : patternInstance.getObjectsByRole().entrySet()) {
                    String role = entry2.getKey();
                    Set<String> objects = entry2.getValue();
                    String hintText = patternName + ":" + role;

                    for(String object : objects){

                        if(psiElement.textMatches(object)) {
                            LineExtensionInfo lineExtensionInfo = new LineExtensionInfo(hintText, JBColor.GRAY, null, null, 8);
                            collection.add(lineExtensionInfo);
                            return collection;
                        }
                    }
                }
            }
        }

        return collection;
    }
}
