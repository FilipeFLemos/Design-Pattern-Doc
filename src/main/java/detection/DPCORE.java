package detection;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import models.PatternCandidate;
import models.PatternInstance;
import models.PatternParticipant;
import parser.ProjectASTParser;
import patterns.Pattern;
import patterns.PatternDetectionAlgorithm;
import storage.PluginState;
import storage.ProjectDetails;

import java.io.*;
import java.util.*;

import gui.MainWindow;

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
        String projectPath = projectDetails.getPath() + "/src";

        for (File file : patternDescriptions) {
            Pattern pat = MainWindow.extractPattern(new File(file.getAbsolutePath()));
            ProjectASTParser.parse(projectPath);
            String toolOutput = PatternDetectionAlgorithm.DetectPattern_Results(pat, false);
            runsToolForSpecificPattern(toolOutput);
        }

        return patternInstances;
    }

    private void sendNotification(String notificationText){
        final Application application = ApplicationManager.getApplication();
        application.invokeLater(() -> {
            Notifications.Bus.notify(new Notification("Design Pattern Doc", "Debug", notificationText, NotificationType.INFORMATION));

        });
    }

    private void runsToolForSpecificPattern(String toolOutput){
        String[] lines = toolOutput.split("\\r?\\n");
        if(lines.length == 0){
            return;
        }

        int numCandidates;
        String line = lines[0];

        String number = line.split(": ")[1];
        numCandidates = Integer.parseInt(number);

        if (numCandidates == 0) {
            return;
        }

        ArrayList<PatternCandidate> patternCandidates = new ArrayList<>();

        if (!parsedPatternCandidates(lines, numCandidates, patternCandidates)) {
            return;
        }

        groupPatternObjects(patternInstances, patternCandidates);
    }

    private boolean parsedPatternCandidates(String[] lines, int numCandidates, ArrayList<PatternCandidate> patternCandidates) {
        String line;
        int index = 2;
        for (int i = 0; i < numCandidates; i++, index++) {
            patternName = retrievePatternName(lines[index]);
            Set<PatternParticipant> patternParticipants = new HashSet<>();
            Set<String> roles = new HashSet<>();

            for(index++; index < lines.length; index++){
                line = lines[index];
                if(line.equals("")){
                    break;
                }

                String[] parts = line.split("\\(");
                String role = parts[1].split("\\)")[0];
                String object = line.split(": ")[1];
                patternParticipants.add(new PatternParticipant(object, role));
                roles.add(role);
            }

            patternCandidates.add(new PatternCandidate(patternParticipants, roles));
        }
        return true;
    }

    private String retrievePatternName(String patternNameLine){
        String[] parts = patternNameLine.split("Pattern ");
        return parts[1].split(":")[0];
    }

    private void groupPatternObjects(Set<PatternInstance> patternInstances, ArrayList<PatternCandidate> patternCandidates) {
        PatternInstance currentPatternInstance = null;
        PatternCandidate currentPatternCandidate = null;
        boolean unsavedPatternInstance = false;

        for (PatternCandidate candidate : patternCandidates) {
            if (isTheFirstCandidate(currentPatternInstance)) {
                currentPatternInstance = new PatternInstance(patternName, candidate);
                currentPatternCandidate = candidate;
                unsavedPatternInstance = true;
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
}
