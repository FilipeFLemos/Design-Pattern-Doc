package refactorings;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.listeners.RefactoringElementAdapter;
import detection.PatternSuggestions;
import org.jetbrains.annotations.NotNull;
import storage.PluginState;
import storage.ProjectDetails;
import storage.ProjectPersistedState;

public class ClassNameRefactoringListener extends RefactoringElementAdapter {

    private String oldName;

    public ClassNameRefactoringListener(String oldName) {
        this.oldName = oldName;
    }

    @Override
    protected void elementRenamedOrMoved(@NotNull PsiElement newElement) {
        try {
            String newFullName = getNewFullName(newElement);
            updateClassNameInSuggestionsAndPersistedState(oldName, newFullName);
        } catch (NullPointerException ignored) {

        }
    }

    @Override
    public void undoElementMovedOrRenamed(@NotNull PsiElement newElement, @NotNull String oldQualifiedName) {
        try {
            String newFullName = getNewFullName(newElement);
            updateClassNameInSuggestionsAndPersistedState(newFullName, oldQualifiedName);
        } catch (NullPointerException ignored) {

        }
    }

    private String getNewFullName(PsiElement newElement) throws NullPointerException {
        PsiClass psiClass = (PsiClass) newElement;
        return psiClass.toString().split(":")[1];
    }

    private void updateClassNameInSuggestionsAndPersistedState(String oldName, String newName) {
        PluginState pluginState = PluginState.getInstance();
        ProjectDetails projectDetails = pluginState.getProjectDetails();
        ProjectPersistedState projectPersistedState = projectDetails.getActiveProjectPersistedState();
        PatternSuggestions patternSuggestions = pluginState.getPatternSuggestions();

        projectPersistedState.updateClassNameInPersistedState(oldName, newName);
        projectDetails.updateAllUmlWithFollowingClassName(newName);
        patternSuggestions.updateClassNameInSuggestions(oldName, newName);
    }
}
