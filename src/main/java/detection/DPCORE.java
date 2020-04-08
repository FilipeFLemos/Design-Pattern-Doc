package detection;

import models.PatternCandidate;
import models.PatternInstance;
import models.PatternParticipant;
import storage.PluginState;
import storage.ProjectDetails;

import java.io.*;
import java.net.URL;
import java.util.*;

public class DPCORE implements DetectionTool {

    private String patternName = "";
    private Set<PatternInstance> patternInstances;
    private ArrayList<File> patternDescriptions;

    public DPCORE() {
        patternInstances = new HashSet<>();
        patternDescriptions = PluginState.getInstance().getPatternDescriptions();
    }

    @Override
    public Set<PatternInstance> scanForPatterns() {

        ProjectDetails projectDetails = PluginState.getInstance().getProjectDetails();
        String projectPath = projectDetails.getPath();
        projectPath = " -project=\"" + projectPath + "/src\" ";

        String execJar = getExecuteJAR();

        for (File file : patternDescriptions) {
            String patternPath = "-pattern=\"" + file.getAbsolutePath() + "\"";
            try {
                runsToolForSpecificPattern(execJar + projectPath + patternPath + " -group=false");
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }

        return patternInstances;
    }

    private String getExecuteJAR() {
        URL resourcesURL = this.getClass().getClassLoader().getResource("patterns/");
        String resourcesPath = resourcesURL.getPath().replace("file:/", "");
        String[] pathSplit = resourcesPath.split("lib");
        String jarPath = pathSplit[0] + "lib/DP-CORE.jar";
        return "java -jar \"" + jarPath + "\"";
    }

    private void runsToolForSpecificPattern(String command) throws Exception {
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

    private void groupPatternObjects(Set<PatternInstance> patternInstances, ArrayList<PatternCandidate> patternCandidates) {
        PatternInstance currentPatternInstance = null;
        PatternCandidate currentPatternCandidate = null;
        boolean unsavedPatternInstance = false;

        for (PatternCandidate candidate : patternCandidates) {
            if (isTheFirstCandidate(currentPatternInstance)) {
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

    private boolean isTheFirstCandidate(PatternInstance currentPatternInstance) {
        return currentPatternInstance == null;
    }

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

    private boolean belongToSamePattern(PatternCandidate thisPatternCandidate, PatternCandidate thatPatternCandidate) {
        int objectsInCommon = 0;
        Set<PatternParticipant> thisPatternParticipants = thisPatternCandidate.getPatternParticipants();
        Set<PatternParticipant> thatPatternParticipants = thatPatternCandidate.getPatternParticipants();
        for (PatternParticipant thisPatternParticipant : thisPatternParticipants) {

            for (PatternParticipant thatPatternParticipant : thatPatternParticipants) {
                if (thisPatternParticipant.equals(thatPatternParticipant)) {
                    objectsInCommon++;
                }
            }
        }

        return objectsInCommon >= 2;
    }

    private void discardLine(BufferedReader in) throws IOException {
        in.readLine();
    }
    
    private String retrievePatternName(BufferedReader in) throws IOException {
        String line = in.readLine();
        if (line == null) {
            return "";
        }

        String[] parts = line.split("Pattern ");
        return parts[1].split(":")[0];
    }
}
