package detection;


import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import models.PatternInstance;
import org.jetbrains.annotations.NotNull;
import storage.PluginState;

import java.util.*;

public class PatternSuggestionsInspection extends AbstractBaseJavaLocalInspectionTool {

    private Map<String, Set<PatternInstance>> availableSuggestions;

    public PatternSuggestionsInspection(){
        PatternSuggestions patternSuggestions = PluginState.getInstance().getPatternSuggestions();
        availableSuggestions = patternSuggestions.getAvailableSuggestions();
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new JavaElementVisitor() {

            @Override
            public void visitElement(PsiElement element) {

                String[] parsedObject = element.toString().split(":");
                if(parsedObject.length != 2 || !parsedObject[0].equals("PsiIdentifier")){
                    return;
                }

                String className = parsedObject[1];
                if(!element.textMatches(className)){
                    return;
                }

                if(!availableSuggestions.containsKey(className)){
                    return;
                }

                Set<PatternInstance> patternInstancesAvailableForObject = availableSuggestions.get(className);
                for(PatternInstance patternInstance : patternInstancesAvailableForObject){
                    String patternName = patternInstance.getPatternName();
                    Map<String, Set<String>> objectRoles = patternInstance.getObjectRoles();

                    if (!objectRoles.containsKey(className)) {
                        continue;
                    }

                    Set<String> roles = objectRoles.get(className);
                    StringBuilder stringBuilder = new StringBuilder();
                    int i = 0;
                    for(String role : roles){
                        stringBuilder.append(role);
                        if(i != roles.size() - 1){
                            stringBuilder.append(", ");
                        }
                        i++;
                    }

                    String suggestionText = "We believe that this class plays the role(s) " + stringBuilder.toString() +" of the " + patternName + " Design Pattern.";
                    PatternSuggestionQuickFix patternSuggestionQuickFix = new PatternSuggestionQuickFix(patternInstance);

                    holder.registerProblem(element, suggestionText, patternSuggestionQuickFix);
                }
            }
        };
    }

}
