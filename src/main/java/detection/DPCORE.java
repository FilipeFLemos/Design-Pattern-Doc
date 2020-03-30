package detection;

import models.PatternCandidate;
import models.PatternInstance;
import models.PatternParticipant;
import storage.PluginState;
import storage.ProjectDetails;
import utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

public class DPCORE implements DetectionTool {

    private String patternName = "";
    private Set<PatternInstance> patternInstances;
    private ArrayList<String> patternFiles;

    public DPCORE(){
        patternInstances = new HashSet<>();
        patternFiles = Utils.getPatternFiles();
    }

    @Override
    public Set<PatternInstance> scanForPatterns() {

        ProjectDetails projectDetails = PluginState.getInstance().getProjectDetails();
        String projectPath = projectDetails.getPath().replace("/","\\");
        projectPath = " -project=\"" + projectPath + "\\src\" ";

        String execJar = getExecuteJAR();
        String patternsDirPath = getPatternsDirPath();

        for(String patternFile : patternFiles){
            String patternPath = patternsDirPath + patternFile + "\"";

            try {
                runProcess(execJar + projectPath + patternPath + " -group=false");
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }

        System.out.println("Finished scanning!");

        return patternInstances;
    }

    private String getExecuteJAR(){
        URL resourcesURL = this.getClass().getClassLoader().getResource("patterns/");
        String resourcesPath = resourcesURL.getPath().replace("file:/","");
        String[] pathSplit = resourcesPath.split("lib");
        String jarPath = pathSplit[0] + "lib/DP-CORE.jar";
        jarPath = jarPath.replace("/","\\");
        return "java -jar \""+ jarPath +"\"";
    }

    private String getPatternsDirPath(){
        URL resourcesURL = this.getClass().getClassLoader().getResource("patterns/");
        String resourcesPath = resourcesURL.getPath().replace("file:/","");
        String[] pathSplit = resourcesPath.split("build");
        String patternsPath = pathSplit[0] + "build/resources/main/patterns/";
        patternsPath = patternsPath.replace("/","\\");
        return "-pattern=\"" + patternsPath;
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
                currentPatternInstance.updatePatternParticipantsContainers(candidate.getPatternParticipants());
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

            Set<PatternParticipant> patternParticipants = new HashSet<>();
            Set<String> roles = new HashSet<>();

            boolean retrievedAllRoles = false;
            while (!retrievedAllRoles) {
                line = in.readLine();
                if (line == null || line.equals("")) {
                    retrievedAllRoles = true;
                } else {
                    String[] parts = line.split("\\(");
                    String role = parts[1].split("\\)")[0];
                    String object = line.split(": ")[1];
                    patternParticipants.add(new PatternParticipant(object, role));
                    roles.add(role);
                }
            }

            patternCandidates.add(new PatternCandidate(patternParticipants, roles));
        }
        return true;
    }

    /**
     * Checks if two pattern candidates belong to the same pattern. This is done by checking if at least two objects
     * play the same roles in both candidates.
     *
     * @param thisPatternCandidate               - One of the candidates
     * @param thatPatternCandidate - The other candidate
     * @return true if they belong to the same pattern and false if otherwise
     */
    private boolean belongToSamePattern(PatternCandidate thisPatternCandidate, PatternCandidate thatPatternCandidate) {
        int objectsInCommon = 0;
        Set<PatternParticipant> thisPatternParticipants = thisPatternCandidate.getPatternParticipants();
        Set<PatternParticipant> thatPatternParticipants = thatPatternCandidate.getPatternParticipants();
        for(PatternParticipant thisPatternParticipant : thisPatternParticipants){

            for(PatternParticipant thatPatternParticipant : thatPatternParticipants){
                if(thisPatternParticipant.equals(thatPatternParticipant)){
                    objectsInCommon++;
                }
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
