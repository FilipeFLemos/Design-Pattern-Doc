package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import models.DesignPattern;
import models.PatternInstance;
import models.PatternParticipant;
import storage.PluginState;
import storage.ProjectDetails;
import storage.ProjectPersistedState;
import ui.EditDocumentationDialog;
import ui.MyToolWindowFactory;
import utils.Utils;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static utils.Utils.updatePatternSuggestions;

public class DocumentPatternByRightClickingElementAction extends AnAction {

    private String className;
    private ProjectPersistedState projectPersistedState;

    @Override
    public void update(AnActionEvent e) {
        PsiElement psiElement = e.getData(LangDataKeys.PSI_ELEMENT);
        if (psiElement instanceof PsiClass) {
            className = psiElement.toString().split(":")[1];
            e.getPresentation().setEnabledAndVisible(true);
        }
        else if(psiElement instanceof PsiMethod){
            boolean visibility = ((PsiMethod) psiElement).getReturnType() == null;
            className = psiElement.toString().split(":")[1];
            e.getPresentation().setEnabledAndVisible(visibility);
        }
        else{
            e.getPresentation().setEnabledAndVisible(false);
        }
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        DesignPattern defaultDesignPattern = getDefaultDesignPattern();
        ConcurrentHashMap<String, PatternInstance> patternInstanceById = getPatternInstanceById();
        String id = Utils.generatePatternInstanceId(patternInstanceById);
        PatternInstance patternInstance = generateDefaultPatternInstance(defaultDesignPattern);
        projectPersistedState.storePatternInstanceIfAbsent(id, patternInstance);
        updatePatternSuggestions(patternInstance);
        MyToolWindowFactory.updateWindow(new EditDocumentationDialog(true, className));
    }

    protected ConcurrentHashMap<String, PatternInstance> getPatternInstanceById() {
        ProjectDetails projectDetails = PluginState.getInstance().getProjectDetails();
        projectPersistedState = projectDetails.getActiveProjectPersistedState();
        return projectPersistedState.getPatternInstanceById();
    }

    private DesignPattern getDefaultDesignPattern(){
        Set<DesignPattern> supportedDesignPatterns = PluginState.getInstance().getSupportedDesignPatterns();
        for (DesignPattern designPattern : supportedDesignPatterns) {
            return designPattern;
        }
        return null;
    }

    private PatternInstance generateDefaultPatternInstance(DesignPattern defaultDesignPattern){
        String intent = "";
        String patternName = defaultDesignPattern.getName();
        Set<String> roles = defaultDesignPattern.getRoles();
        String role = getDefaultRole(roles);

        Set<PatternParticipant> patternParticipants = new HashSet<>();
        patternParticipants.add(new PatternParticipant(className, role));

        return new PatternInstance(patternName, intent, roles, patternParticipants);
    }

    private String getDefaultRole(Set<String> roles){
        String defaultRole = "";
        for(String role : roles){
            defaultRole = role;
            break;
        }
        return defaultRole;
    }
}
