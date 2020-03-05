package inspections;


import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import models.PatternInstance;
import org.jetbrains.annotations.NotNull;
import storage.PluginState;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PatternSuggestionsInspection extends AbstractBaseJavaLocalInspectionTool {

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new JavaElementVisitor() {

            @Override
            public void visitElement(PsiElement element) {

                PluginState pluginState = (PluginState) PluginState.getInstance();
                HashSet<PatternInstance> suggestions = pluginState.getHints();

                for (PatternInstance patternInstance : suggestions) {
                    String patternName = patternInstance.getPatternName();

                    for (Map.Entry<String, Set<String>> entry2 : patternInstance.getRoleObjects().entrySet()) {
                        String role = entry2.getKey();
                        Set<String> objects = entry2.getValue();
                        String suggestionText = "We believe that this class plays the role " + role +" of the Design Pattern " + patternName + ".";

                        for(String object : objects){

                            if (element.textMatches(object)) {
                                holder.registerProblem(element, suggestionText);
                                //TODO: quick fix
                            }
                        }
                    }
                }
            }
        };
    }

}
