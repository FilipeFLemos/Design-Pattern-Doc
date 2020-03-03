package ui;

import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.openapi.editor.LineExtensionInfo;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.ui.JBColor;
import models.PatternInstance;
import org.jetbrains.annotations.Nullable;
import storage.PluginState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PatternDocumentation implements DocumentationProvider{

    @Nullable
    @Override
    public String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
        return getDocumentationText(element);
    }

    @Nullable
    @Override
    public List<String> getUrlFor(PsiElement element, PsiElement originalElement) {
        return new ArrayList<>();
    }


    @Nullable
    @Override
    public String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        return getDocumentationText(element);
    }

    @Nullable
    @Override
    public PsiElement getDocumentationElementForLookupItem(PsiManager psiManager, Object object, PsiElement element) {
        return null;
    }

    @Nullable
    @Override
    public PsiElement getDocumentationElementForLink(PsiManager psiManager, String link, PsiElement context) {
        return null;
    }

    private String getDocumentationText(PsiElement psiElement){

        PluginState pluginState = (PluginState) PluginState.getInstance();
        ConcurrentHashMap<String, PatternInstance> persistedPatternInstances = pluginState.getState().getPatternInstanceById();
        if(persistedPatternInstances == null)
            return null;

        for (Map.Entry<String, PatternInstance> entry : persistedPatternInstances.entrySet()) {
            PatternInstance patternInstance = entry.getValue();
            String patternName = patternInstance.getPatternName();

            for (Map.Entry<String, Set<String>> entry2 : patternInstance.getObjectsByRole().entrySet()) {
                String role = entry2.getKey();
                Set<String> objects = entry2.getValue();

                for(String object : objects){

                    if(psiElement.textMatches(object)) {
                        StringBuilder documentationText = new StringBuilder();
                        documentationText.append("This class plays the role ").append(role).append("on the Design Pattern ").append(patternName).append(".");

                        String patternIntent = patternInstance.getIntent();
                        if(patternIntent != null){
                            documentationText.append("\n\n").append("Intent: ").append(patternIntent);
                        }

                        String collaborations = patternInstance.getCollaborations();
                        if(collaborations != null){
                            documentationText.append("\n\n").append("Collaborations: ").append(collaborations);
                        }

                        //TODO: Roles

                        return documentationText.toString();
                    }
                }
            }
        }

        return null;
    }

}
