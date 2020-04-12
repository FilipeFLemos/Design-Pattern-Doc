package visualization;

import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import models.PatternInstance;
import org.jetbrains.annotations.Nullable;
import storage.PluginState;
import storage.ProjectDetails;
import storage.ProjectPersistedState;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PatternDocumentation implements DocumentationProvider {

    private String documentationText;
    private String className;
    private StringBuilder documentationTextBuilder;
    private ConcurrentHashMap<String, PatternInstance> persistedPatternInstances;

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

    private String getDocumentationText(PsiElement psiElement) {

        try {
            documentationText = null;
            documentationTextBuilder = new StringBuilder();
            className = psiElement.toString().split(":")[1];
            persistedPatternInstances = getPersistedPatternInstances();

            generatePatternInstancesDocumentationForClass();
            if (!documentationTextBuilder.toString().equals("")) {
               documentationText = documentationTextBuilder.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return documentationText;
    }

    private ConcurrentHashMap<String, PatternInstance> getPersistedPatternInstances() throws NullPointerException {
        ProjectDetails projectDetails = PluginState.getInstance().getProjectDetails();
        ProjectPersistedState projectPersistedState = projectDetails.getActiveProjectPersistedState();
        return projectPersistedState.getPatternInstanceById();
    }

    private void generatePatternInstancesDocumentationForClass() {
        for (Map.Entry<String, PatternInstance> patternInstanceEntry : persistedPatternInstances.entrySet()) {
            PatternInstance patternInstance = patternInstanceEntry.getValue();
            String patternName = patternInstance.getPatternName();

            Map<String, Set<String>> objectRoles = patternInstance.getObjectRoles();
            try {
                classPlaysRoleInPatternInstance(objectRoles);
                includeSeparationBetweenPatternInstances();
                includeClassPlayedRoles(objectRoles);
                includePatternName(patternName);
                includePatternInstanceIntent(patternInstance);
                includePatternInstanceUML(patternInstanceEntry.getKey());
                includeSeparationBetweenPatternInstanceFields();
            } catch (Exception ignored) {

            }
        }
    }

    private void includePatternInstanceUML(String patternInstanceId) {
        ProjectDetails projectDetails = PluginState.getInstance().getProjectDetails();
        String path = projectDetails.getPatternInstanceUmlFilePath(patternInstanceId);
        File file = new File(path);
        BufferedImage image = null;
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int width = image.getWidth();
        int height = image.getHeight();
        String imageDiv = "\n<br>\n<br>\n" +
        "<img width=\""+width+"\" height=\""+(height)+"\"src=\"file:/" +  file.getAbsolutePath() + "\">\n";
        documentationTextBuilder.append(imageDiv);
    }

    private void classPlaysRoleInPatternInstance(Map<String, Set<String>> objectRoles) throws NullPointerException {
        if (!objectRoles.containsKey(className)) {
            throw new NullPointerException();
        }
    }

    private void includeClassPlayedRoles(Map<String, Set<String>> objectRoles) {
        documentationTextBuilder.append("This class plays the role(s) <b><u>");
        Set<String> roles = objectRoles.get(className);
        includeSetObjectsSeparatedByComma(roles);
    }

    private void includeSeparationBetweenPatternInstances() {
        if (!documentationTextBuilder.toString().equals("")) {
            documentationTextBuilder.append("\n<br>");
        }
    }

    private void includePatternName(String patternName) {
        documentationTextBuilder.append("</b></u> of the <b><u>").append(patternName).append("</b></u> Design Pattern.");
    }

    private void includePatternInstanceIntent(PatternInstance patternInstance) {
        String patternIntent = patternInstance.getIntent();
        if (!patternIntent.equals("")) {
            includeSeparationBetweenPatternInstanceFields();
            documentationTextBuilder.append("<b>Intent: </b>").append(patternIntent);
        }
    }

    private void includeSeparationBetweenPatternInstanceFields() {
        documentationTextBuilder.append("\n\n<br><br>");
    }

    private void includeSetObjectsSeparatedByComma(Set<String> objectsSet) {
        int i = 0;
        for (String object : objectsSet) {
            documentationTextBuilder.append(object);
            if (i != objectsSet.size() - 1) {
                documentationTextBuilder.append(", ");
            }
            i++;
        }
    }
}
