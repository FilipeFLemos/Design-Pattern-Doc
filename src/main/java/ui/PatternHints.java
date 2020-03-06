package ui;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorLinePainter;
import com.intellij.openapi.editor.LineExtensionInfo;
import com.intellij.openapi.editor.markup.EffectType;
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

import java.awt.*;
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

        PluginState pluginState = (PluginState) PluginState.getInstance();
        ConcurrentHashMap<String, PatternInstance> hints = pluginState.getState().getPatternInstanceById();
        if(hints == null)
            return collection;

        StringBuilder lineHint = new StringBuilder();
        lineHint.append(" ");

        for(int i = offsetStart; i <= offsetEnd; i++){
            PsiElement psiElement = psiFile.findElementAt(i);
            if(psiElement == null)
                continue;

            String className = psiElement.getText();
            boolean addedClassName = false;

            for (Map.Entry<String, PatternInstance> entry : hints.entrySet()) {
                PatternInstance patternInstance = entry.getValue();
                String patternName = patternInstance.getPatternName();

                Map<String, Set<String>> objectRoles = patternInstance.getObjectRoles();
                if(!objectRoles.containsKey(className)){
                    continue;
                }

                if(!addedClassName) {
                    lineHint.append("  ").append(className).append(" -> ");
                    addedClassName = true;
                }
                else{
                    lineHint.append(", ");
                }

                Set<String> roles = objectRoles.get(psiElement.getText());
                int index = 0;
                for(String role : roles){
                    lineHint.append(patternName).append(":").append(role);

                    if(index != roles.size()-1){
                        lineHint.append(", ");
                    }
                    index++;
                }
            }

            i += psiElement.getTextLength();
        }

        LineExtensionInfo lineExtensionInfo = new LineExtensionInfo(lineHint.toString(), JBColor.CYAN, null, null, Font.ITALIC);
        collection.add(lineExtensionInfo);
        return collection;
    }
}
