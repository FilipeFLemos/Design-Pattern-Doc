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
    @Override
    public void actionPerformed(AnActionEvent e) {
        try {
            runProcess("java -Dfile.encoding=windows-1252 -jar \"C:\\Users\\filip\\IdeaProjects\\DP-CORE\\out\\artifacts\\DP_CORE_jar\\DP-CORE.jar\" -project=\"C:\\Users\\filip\\IdeaProjects\\DP-CORE\\examples\\Abstract Factory Example\" -pattern=\"C:\\Users\\filip\\IdeaProjects\\DP-CORE\\patterns\\Abstract Factory.pattern\" -group=false");
        } catch (Exception e1) {
            e1.printStackTrace();
        }
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

        Map<Integer, PatternInstance> patternInstanceById;
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
        System.out.println(patternCandidates.size());

        for(PatternCandidate candidate : patternCandidates)
        {

        }
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
