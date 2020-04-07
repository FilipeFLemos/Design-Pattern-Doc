package utils;

import models.ClassLink;
import models.DesignPattern;
import models.PatternInstance;
import net.sourceforge.plantuml.SourceStringReader;
import org.jetbrains.annotations.NotNull;
import storage.PluginState;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PlantUmlHelper {

    private String umlFilePath;
    private Set<DesignPattern> supportedDesignPatterns;
    private Set<String> roles;
    private String patternName;
    private StringBuilder stringBuilder;

    public PlantUmlHelper(PatternInstance patternInstance){
        setVariables(patternInstance);
        init(patternInstance);
    }

    private void setVariables(PatternInstance patternInstance){
        supportedDesignPatterns = PluginState.getInstance().getSupportedDesignPatterns();
        roles = patternInstance.getRoleObjects().keySet();
        patternName = patternInstance.getPatternName();
        stringBuilder = new StringBuilder();
    }

    private void init(PatternInstance patternInstance) {
        createPlantUMLString(patternInstance);
        runPlantUmlOnPath();
    }

    private void createPlantUMLString(PatternInstance patternInstance) {
        includePlantUmlHeader();
        includeObjects(patternInstance);
        includeRelations(patternInstance);
        includePlantUmlClose();
    }

    private void includePlantUmlHeader() {
        stringBuilder.append("@startuml").append("\n").append("!pragma graphviz_dot jdot");
    }

    private void includeObjects(PatternInstance patternInstance) {
        for(Map.Entry<String, Set<String>> entry : patternInstance.getObjectRoles().entrySet()){
            String object = entry.getKey();
            Set<String> roles = entry.getValue();
            boolean isAbstract = false, isInterface = false;

            for(String role : roles){
                if(role.contains("Abstract")){
                    isAbstract = true;
                    break;
                }
                else if(role.contains("Interface")){
                    isInterface = true;
                    break;
                }
            }

            if(isAbstract){
                stringBuilder.append("\n").append("abstract class ").append(object);
            }else if(isInterface){
                stringBuilder.append("\n").append("interface ").append(object);
            }else{
                stringBuilder.append("\n").append("class ").append(object);
            }

            stringBuilder.append(" << ");
            int i = 0;
            for(String role : roles){
                stringBuilder.append(role);
                if(i != roles.size()-1){
                    stringBuilder.append(", ");
                }
                i++;
            }
            stringBuilder.append(" >>");
        }
    }

    private void includeRelations(PatternInstance patternInstance) {
        Map<String, Set<String>> roleObjects = patternInstance.getRoleObjects();
        DesignPattern designPattern = getDesignPattern();
        List<ClassLink> rolesLinks = designPattern.getRolesLinks();
        for(ClassLink classLink : rolesLinks){
            String role1 = classLink.getRole1();
            String role2 = classLink.getRole2();
            //TODO LINK correcto
            Set<String> role1Objects = roleObjects.get(role1);
            if(role1Objects == null){
                role1Objects = new HashSet<>();
            }
            Set<String> role2Objects = roleObjects.get(role2);
            if(role2Objects == null){
                role1Objects = new HashSet<>();
            }
            for(String role1Object : role1Objects){
                for(String role2Object : role2Objects){
                    stringBuilder.append("\n").append(role2Object).append(" -- ").append(role1Object);
                }
            }
        }
    }

    private void includePlantUmlClose(){
        stringBuilder.append("\n").append("@enduml");
    }

    private DesignPattern getDesignPattern(){
        DesignPattern designPattern = new DesignPattern();
        for(DesignPattern supportedDesignPattern : supportedDesignPatterns){
            String supportedPatternName = supportedDesignPattern.getName();
            if(supportedPatternName.equals(patternName)){
                designPattern = supportedDesignPattern;
                break;
            }
        }
        return designPattern;
    }

    private void runPlantUmlOnPath() {

        try {
            File file = File.createTempFile("temp", ".png");
            file.deleteOnExit();
            umlFilePath = file.getAbsolutePath();
            OutputStream png = new FileOutputStream(file);
            SourceStringReader reader = new SourceStringReader(stringBuilder.toString());
            reader.outputImage(png).getDescription();
            png.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUmlFilePath() {
        return umlFilePath;
    }
}
