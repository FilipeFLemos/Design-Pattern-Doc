package visualization;

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
    private Set<String> addedObjects, nameRoleAdded;
    private Map<String, Integer> numberEntriesByNameRoleAdded;

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

        addedObjects = new HashSet<>();
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
            PsiElement psiElement = psiFile.findElementAt(i);

            if(psiElement != null){
                String objectName = psiElement.getText();
                if(objectName.equals("import")){
                    break;
                }

                if(!addedObjects.contains(objectName)) {
                    generatePatternHintsForClass(objectName);
                    addedObjects.add(objectName);
                }
                i += objectName.length();
            }
        }
    }

    private void generatePatternHintsForClass(String className) {
        boolean alreadyAddedPatternInstanceForClassName = false;
        nameRoleAdded = new HashSet<>();
        numberEntriesByNameRoleAdded = new HashMap<>();

        StringBuilder patternHintsForClass = new StringBuilder();
        for (Map.Entry<String, PatternInstance> entry : patternInstanceById.entrySet()) {
            PatternInstance patternInstance = entry.getValue();
            String patternName = patternInstance.getPatternName();

            Map<String, Set<String>> objectRoles = patternInstance.getObjectRoles();
            if (!objectRoles.containsKey(className)) {
                continue;
            }

            if (!alreadyAddedPatternInstanceForClassName) {
                char rightArrow = '\u2192';
                patternHintsForClass.append("    ").append(className).append(" ").append(rightArrow).append(" ");
                alreadyAddedPatternInstanceForClassName = true;
            }

            String nameAndRoles = getPatternNameAndClassRoles(className, patternName, objectRoles);

            nameRoleAdded.add(nameAndRoles);
            int times = 1;
            if(numberEntriesByNameRoleAdded.containsKey(nameAndRoles)){
                times += numberEntriesByNameRoleAdded.get(nameAndRoles);
            }
            numberEntriesByNameRoleAdded.put(nameAndRoles, times);
        }

        int index = 0;
        for(String nameRole : nameRoleAdded){
            patternHintsForClass.append(nameRole);
            int times = numberEntriesByNameRoleAdded.get(nameRole);
            if(times > 1){
                patternHintsForClass.append("(").append(times).append("x)");
            }

            if (index != nameRoleAdded.size() - 1) {
                patternHintsForClass.append("; ");
            }
            index ++;
        }

        String patternHint = patternHintsForClass.toString();

        if(!patternHint.equals("")){
            lineHint.append(patternHint);
        }


    }

    private String getPatternNameAndClassRoles(String className, String patternName, Map<String, Set<String>> objectRoles) {
        Set<String> roles = objectRoles.get(className);
        int index = 0;
        StringBuilder nameAndRoles = new StringBuilder();
        nameAndRoles.append(patternName).append(":");

        for (String role : roles) {
            nameAndRoles.append(role);

            if (index != roles.size() - 1) {
                nameAndRoles.append(", ");
            }
            index++;
        }
        return nameAndRoles.toString();
    }
}
