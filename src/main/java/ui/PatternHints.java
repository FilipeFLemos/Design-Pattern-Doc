package ui;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorLinePainter;
import com.intellij.openapi.editor.LineExtensionInfo;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiJavaFileImpl;
import com.intellij.ui.JBColor;
import models.PatternInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import storage.PluginState;
import storage.ProjectDetails;
import storage.ProjectPersistedState;

import java.awt.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PatternHints extends EditorLinePainter {

    ConcurrentHashMap<String, PatternInstance> patternInstanceById;
    Collection<LineExtensionInfo> linePatternHints;
    StringBuilder lineHint;
    Set<String> fileClassesName;
    private Project project;
    private PsiFile psiFile;
    private Document document;
    private int lineStart;
    private int lineEnd;

    @Nullable
    @Override
    public Collection<LineExtensionInfo> getLineExtensions(@NotNull Project project, @NotNull VirtualFile file, int lineNumber) {
        linePatternHints = new ArrayList<>();
        try {
            trySetUpPsiFileAndLineBoundaries(project, file, lineNumber);
            generateLineExtensions();
        } catch (Exception ignored) {

        }
        return linePatternHints;
    }

    private void trySetUpPsiFileAndLineBoundaries(Project project, VirtualFile file, int lineNumber) throws NullPointerException {
        setProject(project);
        setPsiFile(file);
        if (!existsPsiFile(psiFile))
            throw new NullPointerException();

        setDocument();
        setLinePositionBoundariesInFile(lineNumber);
    }

    private void generateLineExtensions() throws NullPointerException {
        setPatternInstanceById();
        if (isInvalidPatternInstanceById()) {
            throw new NullPointerException();
        }
        setFileClassesName();

        lineHint = new StringBuilder();
        appendInitialIndentation();
        generatePatternHintsForAllClassesInLine();
        LineExtensionInfo lineExtensionInfo = new LineExtensionInfo(lineHint.toString(), JBColor.CYAN, null, null, Font.ITALIC);
        linePatternHints.add(lineExtensionInfo);
    }

    private void setProject(Project project) {
        this.project = project;
    }

    private void setPsiFile(VirtualFile virtualFile) {
        PsiManager psiManager = PsiManager.getInstance(project);
        this.psiFile = psiManager.findFile(virtualFile);
    }

    private boolean existsPsiFile(PsiFile psiFile) {
        return psiFile != null;
    }

    private void setDocument() {
        this.document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
    }

    private void setLinePositionBoundariesInFile(int lineNumber) {
        setLineStart(lineNumber);
        setLineEnd(lineNumber);
    }

    private void setLineStart(int lineNumber) {
        this.lineStart = document.getLineStartOffset(lineNumber);
    }

    private void setLineEnd(int lineNumber) {
        this.lineEnd = document.getLineEndOffset(lineNumber);
        ;
    }

    private void setPatternInstanceById() {
        ProjectDetails projectDetails = PluginState.getInstance().getProjectDetails();
        ProjectPersistedState projectPersistedState = projectDetails.getActiveProjectPersistedState();
        patternInstanceById = projectPersistedState.getPatternInstanceById();
    }

    private boolean isInvalidPatternInstanceById() {
        return patternInstanceById == null || patternInstanceById.isEmpty();
    }

    private void appendInitialIndentation() {
        lineHint.append(" ");
    }

    private void setFileClassesName() {
        Set<String> fileClassesName = new HashSet<>();
        PsiClass[] psiClasses = ((PsiJavaFileImpl) psiFile).getClasses();

        for (PsiClass psiClass : psiClasses) {
            PsiElement psiElement = psiClass.getOriginalElement();
            String className = psiElement.toString().split(":")[1];
            fileClassesName.add(className);
        }

        this.fileClassesName = fileClassesName;
    }

    private void generatePatternHintsForAllClassesInLine() {
        for (int i = lineStart; i <= lineEnd; i++) {
            try {
                String objectName = getObjectName(i);
                generatePatternHintsForClass(objectName);
                i += objectName.length();
            } catch (Exception ignored) {

            }
        }
    }

    private String getObjectName(int i) throws Exception {
        String elementName;
        elementName = getFileElementName(i);

        if (!isClassDefinedAtCurrentFile(elementName)) {
            elementName = getReferencedClassName(i);
        }

        return elementName;
    }

    private String getFileElementName(int i) throws Exception {
        String objectName;
        PsiElement psiElement = psiFile.findElementAt(i);

        if (psiElement == null) {
            throw new Exception();
        }

        objectName = psiElement.getText();
        return objectName;
    }

    private boolean isClassDefinedAtCurrentFile(String objectName) {
        return fileClassesName.contains(objectName);
    }

    private String getReferencedClassName(int i) throws Exception {
        String elementName;
        PsiReference psiReference = psiFile.findReferenceAt(i);

        if (psiReference == null)
            throw new Exception();

        PsiElement psiElement = psiReference.getElement();
        elementName = psiElement.getText();
        return elementName;
    }

    private void generatePatternHintsForClass(String className) {
        boolean alreadyAddedPatternInstanceForClassName = false;
        for (Map.Entry<String, PatternInstance> entry : patternInstanceById.entrySet()) {
            PatternInstance patternInstance = entry.getValue();
            String patternName = patternInstance.getPatternName();

            Map<String, Set<String>> objectRoles = patternInstance.getObjectRoles();
            if (!objectRoles.containsKey(className)) {
                continue;
            }

            if (!alreadyAddedPatternInstanceForClassName) {
                lineHint.append("  ").append(className).append(" -> ");
                alreadyAddedPatternInstanceForClassName = true;
            } else {
                lineHint.append(", ");
            }

            appendPatternNameAndClassRoles(className, patternName, objectRoles);
        }
    }

    private void appendPatternNameAndClassRoles(String className, String patternName, Map<String, Set<String>> objectRoles) {
        Set<String> roles = objectRoles.get(className);
        int index = 0;

        for (String role : roles) {
            lineHint.append(patternName).append(":").append(role);

            if (index != roles.size() - 1) {
                lineHint.append(", ");
            }
            index++;
        }
    }
}
