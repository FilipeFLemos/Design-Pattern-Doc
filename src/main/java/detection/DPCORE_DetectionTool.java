package detection;

import models.PatternCandidate;
import models.PatternInstance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class DPCORE_DetectionTool extends AbstractDetectionTool {

    private final String executeJAR = "java -Dfile.encoding=windows-1252 -jar \"C:\\Users\\filip\\IdeaProjects\\DP-CORE\\out\\artifacts\\DP_CORE_jar\\DP-CORE.jar\"";
    private final String patternRepoPath = "-pattern=\"C:\\Users\\filip\\IdeaProjects\\DP-CORE\\patterns\\";
    private final String projectPath = " -project=\"C:\\Users\\filip\\IdeaProjects\\ThesisTest\\src\\examples\\Abstract Factory Example\" ";

    private String patternName = "";
    private Set<PatternInstance> patternInstances;

    public DPCORE_DetectionTool(){
        patternInstances = new HashSet<>();
    }

    /**
     * Runs the DP-CORE tool for each of the implemented tool's patterns.
     */
    @Override
    public Set<PatternInstance> scanForPatterns() {

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

        return patternInstances;
    }

    /**
     * Executes the DP-CORE tool for a specific pattern. Aborts if no pattern candidates are found.
     *
     * @param command - The DP-CORE tool command for a specific pattern
     * @throws Exception - Any exception while executing the process or reading the text output
     */
    private void runProcess(String command) throws Exception {
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

        groupPatternObjects(patternInstances, patternCandidates);
    }

    /**
     * Groups all the objects that play a role in the same pattern
     *
     * @param patternInstances - Set with all the pattern instances detected
     * @param patternCandidates   - The list of objects and their roles
     */
    private void groupPatternObjects(Set<PatternInstance> patternInstances, ArrayList<PatternCandidate> patternCandidates) {
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
                patternInstances.add(currentPatternInstance);

                unsavedPatternInstance = false;
                currentPatternInstance = new PatternInstance(patternName, candidate);
                currentPatternCandidate = candidate;
            }
        }

        if (unsavedPatternInstance) {
            patternInstances.add(currentPatternInstance);
        }
    }

    /**
     * Generates a pattern instance ID (map key) and adds the pattern instance to the map.
     * @param patternInstances - Set with all the pattern instances detected
     * @param currentPatternInstance - The pattern instance to be added
     */
//    private static void addPatternInstance(Set<String, PatternInstance> patternInstanceById, PatternInstance currentPatternInstance) {
//        String id = "";
//        do {
//            id = Utils.generateAlphaNumericString();
//        } while (patternInstanceById.containsKey(id));
//
//        patternInstanceById.put(id, currentPatternInstance);
//    }

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
    private boolean parsedPatternCandidates(BufferedReader in, int numCandidates, ArrayList<PatternCandidate> patternCandidates) throws IOException {
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
    private boolean belongToSamePattern(PatternCandidate candidate, PatternCandidate currentPatternCandidate) {
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
    private void discardLine(BufferedReader in) throws IOException {
        in.readLine();
    }

    /**
     * Retrieves the pattern name.
     *
     * @param in - The tool's output text
     * @return the pattern name or the empty string
     * @throws IOException - Any exception retrieved while reading the text output
     */
    private String retrievePatternName(BufferedReader in) throws IOException {
        String line = in.readLine();
        if (line == null) {
            return "";
        }

        String[] parts = line.split("Pattern ");
        return parts[1].split(":")[0];
    }
}