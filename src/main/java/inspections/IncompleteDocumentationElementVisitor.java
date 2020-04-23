package inspections;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiElement;
import models.PatternInstance;
import storage.PluginState;
import storage.ProjectDetails;
import storage.ProjectPersistedState;
import storage.ProjectsPersistedState;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class IncompleteDocumentationElementVisitor extends JavaElementVisitor {

    private ProblemsHolder holder;
    private String object;
    private ConcurrentHashMap<String, PatternInstance> patternInstanceById;

    public IncompleteDocumentationElementVisitor(final ProblemsHolder holder) {
        this.holder = holder;
    }

    @Override
    public void visitElement(PsiElement element) {

        String[] parsedObject = element.toString().split(":");
        if (parsedObject.length != 2 || !isPsiIdentifier(parsedObject[0])) {
            return;
        }

        object = parsedObject[1];
        if (!isElementAClass(element)) {
            return;
        }

        Project project = element.getProject();
        ProjectsPersistedState projectsPersistedState = PluginState.getInstance().getState();

        if (projectsPersistedState != null) {
            ProjectPersistedState persistedState = projectsPersistedState.getProjectState(project.getName());
            patternInstanceById = persistedState.getPatternInstanceById();
            registerIncompleteDocumentationWarning(element);
        }
    }

    private boolean isPsiIdentifier(String s) {
        return s.equals("PsiIdentifier");
    }

    private boolean isElementAClass(PsiElement element) {
        return element.textMatches(object);
    }

    private void registerIncompleteDocumentationWarning(PsiElement element) {

        for(Map.Entry<String, PatternInstance> entry : patternInstanceById.entrySet()){
            String patternInstanceId = entry.getKey();
            PatternInstance patternInstance = entry.getValue();
            Map<String, Set<String>> objectRoles = patternInstance.getObjectRoles();

            if(objectRoles.containsKey(object)){
                Set<String> emptyRoles = patternInstance.getEmptyRoles();

                if (isDocumentationIncomplete(emptyRoles)) {
                    String warningText = "A pattern instance played by this object is incomplete! Consider filling the roles - " + getObjectsSeparatedByComma(emptyRoles) + " - of the " + patternInstance.getPatternName() + " Design Pattern.";
                    EditPatternInstanceQuickFix editPatternInstanceQuickFix = new EditPatternInstanceQuickFix(patternInstanceId);
                    DeletePatternInstanceQuickFix deletePatternInstanceQuickFix = new DeletePatternInstanceQuickFix(patternInstanceId);
                    holder.registerProblem(element, warningText, editPatternInstanceQuickFix, deletePatternInstanceQuickFix);
                }
            }
        }
    }

    private boolean isDocumentationIncomplete(Set<String> emptyRoles){
        return !emptyRoles.isEmpty();
    }

    private String getObjectsSeparatedByComma(Set<String> objectsSet) {
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        for (String object : objectsSet) {
            stringBuilder.append(object);
            if (i != objectsSet.size() - 1) {
                stringBuilder.append(", ");
            }
            i++;
        }
        return stringBuilder.toString();
    }
}
