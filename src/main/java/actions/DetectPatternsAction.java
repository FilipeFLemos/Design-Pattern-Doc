package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import models.PatternCandidate;
import models.PatternInstance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DetectPatternsAction extends AnAction {

    private static Integer patternInstanceID = 0;
    private final String executeJAR = "java -Dfile.encoding=windows-1252 -jar \"C:\\Users\\filip\\IdeaProjects\\DP-CORE\\out\\artifacts\\DP_CORE_jar\\DP-CORE.jar\"";
    private final String patternRepoPath = "-pattern=\"C:\\Users\\filip\\IdeaProjects\\DP-CORE\\patterns\\";
    private final String projectPath = " -project=\"C:\\Users\\filip\\IdeaProjects\\DP-CORE\\examples\\Abstract Factory Example\" ";

    @Override
    public void actionPerformed(AnActionEvent e) {

        scanForPatterns();
    }

    private void scanForPatterns() {

        String pattern = "Abstract Factory.pattern\"";
        try {
            runProcess(executeJAR + projectPath + patternRepoPath + pattern + " -group=false");
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        pattern = "Bridge.pattern\"";
        try {
            runProcess(executeJAR + projectPath + patternRepoPath + pattern + " -group=false");
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        pattern = "Builder.pattern\"";
        try {
            runProcess(executeJAR + projectPath + patternRepoPath + pattern + " -group=false");
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        pattern = "Command.pattern\"";
        try {
            runProcess(executeJAR + projectPath + patternRepoPath + pattern + " -group=false");
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        pattern = "Observer.pattern\"";
        try {
            runProcess(executeJAR + projectPath + patternRepoPath + pattern + " -group=false");
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        pattern = "Visitor.pattern\"";
        try {
            runProcess(executeJAR + projectPath + patternRepoPath + pattern + " -group=false");
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        System.out.println("Finished scanning!");
    }

    private static void runProcess(String command) throws Exception {
        Process pro = Runtime.getRuntime().exec(command);
        BufferedReader in = new BufferedReader(new InputStreamReader(pro.getInputStream()));

        int numCandidates = -1;
        String line = in.readLine();
        if(line == null){
            return;
        }

        String number = line.split(": ")[1];
        numCandidates = Integer.parseInt(number);

        if(numCandidates == 0){
            return;
        }

        discardLine(in);

        Map<Integer, PatternInstance> patternInstanceById = new HashMap<>();
        ArrayList<PatternCandidate> patternCandidates = new ArrayList<>();
        String patternName = "";

        for (int i=0; i < numCandidates; i++)
        {
            patternName = retrievePatternName(in);
            if(patternName.equals("")){
                return;
            }

            Map<String, String> roleClass = new HashMap<>();

            boolean retrievedAllRoles = false;
            while (!retrievedAllRoles)
            {
                line = in.readLine();
                if(line == null || line.equals(""))
                {
                    retrievedAllRoles = true;
                }
                else
                {
                    String[] parts = line.split("\\(");
                    String objectRole = parts[1].split("\\)")[0];
                    String objectClass = line.split(": ")[1];
                    roleClass.put(objectRole, objectClass);
                }
            }

            patternCandidates.add(new PatternCandidate(roleClass));
        }

        pro.waitFor();

        PatternInstance currentPatternInstance = null;
        PatternCandidate currentPatternCandidate = null;
        boolean unsavedPatternInstance = false;

        for(PatternCandidate candidate : patternCandidates)
        {
            if(currentPatternInstance == null)
            {
                currentPatternInstance = new PatternInstance(patternName, candidate);
                currentPatternCandidate = candidate;
                continue;
            }

            if(belongToSamePattern(candidate,currentPatternCandidate))
            {
                currentPatternInstance.addObjectToRole(candidate);
                unsavedPatternInstance = true;
            }
            else
            {
                patternInstanceById.put(patternInstanceID, currentPatternInstance);
                patternInstanceID++;
                unsavedPatternInstance = false;
                currentPatternInstance = new PatternInstance(patternName, candidate);
                currentPatternCandidate = candidate;
            }
        }

        if(unsavedPatternInstance)
        {
            patternInstanceById.put(patternInstanceID, currentPatternInstance);
            patternInstanceID++;
        }

    }

    private static boolean belongToSamePattern(PatternCandidate candidate, PatternCandidate currentPatternCandidate) {
        int objectsInCommon = 0;
        for (Map.Entry<String, String> candidateObjectByRole : candidate.getObjectByRole().entrySet())
        {
            String role1 = candidateObjectByRole.getKey();
            String object1 = candidateObjectByRole.getValue();

            String object2 = currentPatternCandidate.getObjectByRole().get(role1);
            if(object1.equals(object2))
            {
                objectsInCommon++;
            }
        }

        return objectsInCommon >= 2;
    }

    private static void discardLine(BufferedReader in) throws IOException {
        in.readLine();
    }

    private static String retrievePatternName(BufferedReader in) throws IOException{
        String line = in.readLine();
        if(line == null){
            return "";
        }

        String[] parts = line.split("Pattern ");
        return parts[1].split(":")[0];
    }
}
