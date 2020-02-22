package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import models.PatternCandidate;
import models.PatternInstance;
import models.PersistentDataManager;
import utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DetectPatternsAction extends AnAction {

    private final String executeJAR = "java -Dfile.encoding=windows-1252 -jar \"C:\\Users\\filip\\IdeaProjects\\DP-CORE\\out\\artifacts\\DP_CORE_jar\\DP-CORE.jar\"";
    private final String patternRepoPath = "-pattern=\"C:\\Users\\filip\\IdeaProjects\\DP-CORE\\patterns\\";
    private final String projectPath = " -project=\"C:\\Users\\filip\\IdeaProjects\\DP-CORE\\examples\\Abstract Factory Example\" ";

    private static String patternName = "";
    private static Map<String, PatternInstance> patternInstanceById;

    @Override
    public void actionPerformed(AnActionEvent e) {

        patternInstanceById = new HashMap<>();
        scanForPatterns();

        PersistentDataManager persistentDataManager = PersistentDataManager.getInstance();
        for (Map.Entry<String, PatternInstance> entry : patternInstanceById.entrySet()) {
            String id = entry.getKey();
            PatternInstance patternInstance = entry.getValue();
            persistentDataManager.addPatternInstance(id, patternInstance);
        }

    }

    /**
     * Runs the DP-CORE tool for each of the implemented tool's patterns.
     */
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

    /**
     * Executes the DP-CORE tool for a specific pattern. Aborts if no pattern candidates are found.
     *
     * @param command - The DP-CORE tool command for a specific pattern
     * @throws Exception - Any exception while executing the process or reading the text output
     */
    private static void runProcess(String command) throws Exception {
        Process pro = Runtime.getRuntime().exec(command);
        BufferedReader in = new BufferedReader(new InputStreamReader(pro.getInputStream()));

        int numCandidates;
        String line = in.readLine();
        if (line == null) {
            return;
        }

        String number = line.split(": ")[1];
        numCandidates = Integer.parseInt(number);

        if (numCandidates == 0) {
            return;
        }

        discardLine(in);

        ArrayList<PatternCandidate> patternCandidates = new ArrayList<>();

        if (!parsedPatternCandidates(in, numCandidates, patternCandidates)) {
            return;
        }

        pro.waitFor();

        groupPatternObjects(patternInstanceById, patternCandidates);
    }

    /**
     * Groups all the objects that play a role in the same pattern
     *
     * @param patternInstanceById - Maps for each pattern instance ID, the actual pattern instance
     * @param patternCandidates   - The list of objects and their roles
     */
    private static void groupPatternObjects(Map<String, PatternInstance> patternInstanceById, ArrayList<PatternCandidate> patternCandidates) {
        PatternInstance currentPatternInstance = null;
        PatternCandidate currentPatternCandidate = null;
        boolean unsavedPatternInstance = false;


        for (PatternCandidate candidate : patternCandidates) {
            if (currentPatternInstance == null) {
                currentPatternInstance = new PatternInstance(patternName, candidate);
                currentPatternCandidate = candidate;
                continue;
            }

            if (belongToSamePattern(candidate, currentPatternCandidate)) {
                currentPatternInstance.addObjectToRole(candidate);
                unsavedPatternInstance = true;
            } else {
                addPatternInstance(patternInstanceById, currentPatternInstance);

                unsavedPatternInstance = false;
                currentPatternInstance = new PatternInstance(patternName, candidate);
                currentPatternCandidate = candidate;
            }
        }

        if (unsavedPatternInstance) {
            addPatternInstance(patternInstanceById, currentPatternInstance);
        }
    }

    /**
     * Generates a pattern instance ID (map key) and adds the pattern instance to the map.
     * @param patternInstanceById - Maps for each pattern instance ID, the actual pattern instance
     * @param currentPatternInstance - The pattern instance to be added
     */
    private static void addPatternInstance(Map<String, PatternInstance> patternInstanceById, PatternInstance currentPatternInstance) {
        String id = "";
        do {
            id = Utils.generateAlphaNumericString();
        } while (patternInstanceById.containsKey(id));

        patternInstanceById.put(id, currentPatternInstance);
    }

    /**
     * Parses the output from the DP-CORE tool, extracting the roles and objects of each pattern candidate.
     * Also retrieves the name of the pattern where the object is inserted.
     *
     * @param in                - The tool's output text
     * @param numCandidates     - The number of retrieved candidates
     * @param patternCandidates - A list where each element contains the roles and objects of each pattern candidate
     * @return false if it could not parse the pattern name and true if all the candidates were correctly parsed
     * @throws IOException - Any exception retrieved while reading the text output
     */
    private static boolean parsedPatternCandidates(BufferedReader in, int numCandidates, ArrayList<PatternCandidate> patternCandidates) throws IOException {
        String line;
        for (int i = 0; i < numCandidates; i++) {
            patternName = retrievePatternName(in);
            if (patternName.equals("")) {
                return false;
            }

            Map<String, String> roleClass = new HashMap<>();

            boolean retrievedAllRoles = false;
            while (!retrievedAllRoles) {
                line = in.readLine();
                if (line == null || line.equals("")) {
                    retrievedAllRoles = true;
                } else {
                    String[] parts = line.split("\\(");
                    String objectRole = parts[1].split("\\)")[0];
                    String objectClass = line.split(": ")[1];
                    roleClass.put(objectRole, objectClass);
                }
            }

            patternCandidates.add(new PatternCandidate(roleClass));
        }
        return true;
    }

    /**
     * Checks if two pattern candidates belong to the same pattern. This is done by checking if at least two objects
     * play the same roles in both candidates.
     *
     * @param candidate               - One of the candidates
     * @param currentPatternCandidate - The other candidate
     * @return true if they belong to the same pattern and false if otherwise
     */
    private static boolean belongToSamePattern(PatternCandidate candidate, PatternCandidate currentPatternCandidate) {
        int objectsInCommon = 0;
        for (Map.Entry<String, String> candidateObjectByRole : candidate.getObjectByRole().entrySet()) {
            String role1 = candidateObjectByRole.getKey();
            String object1 = candidateObjectByRole.getValue();

            String object2 = currentPatternCandidate.getObjectByRole().get(role1);
            if (object1.equals(object2)) {
                objectsInCommon++;
            }
        }

        return objectsInCommon >= 2;
    }

    /**
     * Discards the following line. Used to ignore the empty lines on the DP-CORE tool's text output.
     *
     * @param in - The tool's output text
     * @throws IOException - Any exception retrieved while reading the text output
     */
    private static void discardLine(BufferedReader in) throws IOException {
        in.readLine();
    }

    /**
     * Retrieves the pattern name.
     *
     * @param in - The tool's output text
     * @return the pattern name or the empty string
     * @throws IOException - Any exception retrieved while reading the text output
     */
    private static String retrievePatternName(BufferedReader in) throws IOException {
        String line = in.readLine();
        if (line == null) {
            return "";
        }

        String[] parts = line.split("Pattern ");
        return parts[1].split(":")[0];
    }
}
