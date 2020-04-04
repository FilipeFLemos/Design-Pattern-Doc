package detection;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiElement;
import models.PatternInstance;
import storage.PluginState;

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

        for (PatternInstance patternInstance : patternInstancesAvailableForObject) {
            registerPatternInstanceDocumentationSuggestion(element, patternInstance);
        }
    }

    private void registerPatternInstanceDocumentationSuggestion(PsiElement element, PatternInstance patternInstance) {
        String patternName = patternInstance.getPatternName();
        String objectRolesText = getObjectRolesText(patternInstance);

        String suggestionText = "We believe that this class plays the role(s) " + objectRolesText + " of the " + patternName + " Design Pattern.";
        PatternSuggestionQuickFix patternSuggestionQuickFix = new PatternSuggestionQuickFix(patternInstance);
        PatternSuggestionIgnoreQuickFix patternSuggestionIgnoreQuickFix = new PatternSuggestionIgnoreQuickFix(patternInstance);
        holder.registerProblem(element, suggestionText, patternSuggestionQuickFix, patternSuggestionIgnoreQuickFix);
    }

    private String getObjectRolesText(PatternInstance patternInstance) {
        Map<String, Set<String>> objectRoles = patternInstance.getObjectRoles();
        Set<String> roles = objectRoles.get(object);
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;

        for (String role : roles) {
            stringBuilder.append(role);
            if (i != roles.size() - 1) {
                stringBuilder.append(", ");
            }
            i++;
        }
        return stringBuilder.toString();
    }
}
