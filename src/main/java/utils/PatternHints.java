package utils;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorLinePainter;
import com.intellij.openapi.editor.LineExtensionInfo;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

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
                return collection;

            if(psiElement.textMatches("PluginState")) {
                LineExtensionInfo lineExtensionInfo = new LineExtensionInfo(" Composite:Component", JBColor.GRAY, null, null, 8);
                collection.add(lineExtensionInfo);
                return collection;
            }
        }

        return collection;
    }
}
