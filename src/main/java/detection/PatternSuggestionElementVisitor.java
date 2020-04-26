package detection;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiElement;
import models.PatternInstance;
import org.jetbrains.annotations.NotNull;
import storage.PluginState;
import utils.Utils;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class PatternSuggestionElementVisitor extends JavaElementVisitor {

    private ProblemsHolder holder;
    private Map<String, Set<PatternInstance>> availableSuggestions;
    private String object;

    public PatternSuggestionElementVisitor(final ProblemsHolder holder) {
        this.holder = holder;
        PatternSuggestions patternSuggestions = PluginState.getInstance().getPatternSuggestions();
        availableSuggestions = patternSuggestions.getAvailableSuggestions();
    }

    @Override
    public void visitElement(PsiElement element) {

        String[] parsedObject = element.toString().split(":");
        if (parsedObject.length != 2 || !isPsiIdentifier(parsedObject[0])) {
            return;
        }

        object = parsedObject[1];
        if (!isElementAClass(element) || !areSuggestionsAvailableForClass()) {
            return;
        }

        registerDocumentationSuggestion(element);
    }

    private boolean isPsiIdentifier(String s) {
        return s.equals("PsiIdentifier");
    }

    private boolean isElementAClass(PsiElement element) {
        return element.textMatches(object);
    }

    private boolean areSuggestionsAvailableForClass() {
        return availableSuggestions.containsKey(object);
    }

    private void registerDocumentationSuggestion(PsiElement element) {
        Set<PatternInstance> patternInstancesAvailableForObject = availableSuggestions.get(object);

        if(patternInstancesAvailableForObject.size() == 1){
            registerWithoutDialogAction(element, patternInstancesAvailableForObject);
        }
        else{
            for (PatternInstance patternInstance : patternInstancesAvailableForObject) {
                registerWithDialog(element, patternInstance, patternInstancesAvailableForObject);
            }
        }
    }

    private void registerWithoutDialogAction(PsiElement element, Set<PatternInstance> patternInstancesAvailableForObject) {
        ArrayList<PatternInstance> patternInstances = new ArrayList<>(patternInstancesAvailableForObject);
        PatternInstance patternInstance = patternInstances.get(0);
        String suggestionText = getSuggestionText(patternInstance);

        PatternSuggestionIgnoreQuickFix patternSuggestionIgnoreQuickFix = new PatternSuggestionIgnoreQuickFix(patternInstance);
        PatternSuggestionQuickFix patternSuggestionQuickFix = new PatternSuggestionQuickFix(patternInstance);
        holder.registerProblem(element, suggestionText, patternSuggestionQuickFix, patternSuggestionIgnoreQuickFix);
    }

    private void registerWithDialog(PsiElement element, PatternInstance patternInstance, Set<PatternInstance> patternInstancesAvailableForObject) {
        String suggestionText = getSuggestionText(patternInstance);

        PatternSuggestionMultipleActionQuickFix patternSuggestionQuickFix = new PatternSuggestionMultipleActionQuickFix(patternInstancesAvailableForObject, object, false);
        PatternSuggestionMultipleActionQuickFix patternSuggestionIgnoreFix = new PatternSuggestionMultipleActionQuickFix(patternInstancesAvailableForObject, object, true);
        holder.registerProblem(element, suggestionText, patternSuggestionQuickFix, patternSuggestionIgnoreFix);
    }

    @NotNull
    private String getSuggestionText(PatternInstance patternInstance) {
        String patternName = patternInstance.getPatternName();
        String objectRolesText = Utils.getObjectRolesText(patternInstance, object);
        return "This class may play the role(s) " + objectRolesText + " of the " + patternName + " Design Pattern.";
    }
}
