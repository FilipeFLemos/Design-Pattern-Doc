package ui;

import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
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

        for (Map.Entry<String, PatternInstance> patternInstanceEntry : persistedPatternInstances.entrySet()) {
            PatternInstance patternInstance = patternInstanceEntry.getValue();
            String patternName = patternInstance.getPatternName();

            Map<String, Set<String>> objectRoles = patternInstance.getObjectRoles();
            if(!objectRoles.containsKey(psiElement.getText())){
                return null;
            }

            StringBuilder documentationText = new StringBuilder();

            Set<String> roles = objectRoles.get(psiElement.getText());
            for(String role : roles){
                documentationText.append("This class plays the role <b><u>").append(role).append("</b></u> of the <b><u>").append(patternName).append("</b></u> Design Pattern.");
            }

            String patternIntent = patternInstance.getIntent();
            if(!patternIntent.equals("")){
                documentationText.append("\n\n<br><br>").append("<b>Intent: </b>").append(patternIntent);
            }

            String collaborations = patternInstance.getCollaborations();
            if(!collaborations.equals("")){
                documentationText.append("\n\n<br><br>").append("<b>Collaborations: </b>").append(collaborations);
            }

            documentationText.append("\n\n<br><br>").append("<b>Roles:</b>");

            Map<String, Set<String>> objectsByRole = patternInstance.getRoleObjects();
            for (Map.Entry<String, Set<String>> objectsByRoleEntry : objectsByRole.entrySet()) {
                String patternRole = objectsByRoleEntry.getKey();
                Set<String> objectsPlayingRole = objectsByRoleEntry.getValue();

                documentationText.append("\n<br><u>").append(patternRole).append("</u> - ");

                int i = 0;
                for(String objectPlayingRole : objectsPlayingRole){
                    if(i == objectsPlayingRole.size() - 1){
                        documentationText.append(objectPlayingRole);
                    }
                    else {
                        documentationText.append(objectPlayingRole).append(", ");
                    }
                }
            }

            return documentationText.toString();
        }

        return null;
    }

}
