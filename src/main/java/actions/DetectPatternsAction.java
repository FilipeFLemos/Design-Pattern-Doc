package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DetectPatternsAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        try {
            runProcess("java -version");
            //runProcess("C:\\Program Files\\Java\\jdk-11.0.3\\bin\\java -Dfile.encoding=windows-1252 -jar \"C:\\Users\\filip\\IdeaProjects\\DP-CORE\\out\\artifacts\\DP_CORE_jar\\DP-CORE.jar\" -project=\"C:\\Users\\filip\\IdeaProjects\\DP-CORE\\examples\\Abstract Factory Example\" -pattern=\"C:\\Users\\filip\\IdeaProjects\\DP-CORE\\patterns\\Abstract Factory.pattern\" -group=false");
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    private static void runProcess(String command) throws Exception {
        Process pro = Runtime.getRuntime().exec(command);
        printLines(command + " stdout:", pro.getInputStream());
        printLines(command + " stderr:", pro.getErrorStream());
        pro.waitFor();
        System.out.println(command + " exitValue() " + pro.exitValue());
    }

    private static void printLines(String cmd, InputStream ins) throws Exception {
        String line = null;
        BufferedReader in = new BufferedReader(
                new InputStreamReader(ins));
        while ((line = in.readLine()) != null) {
            System.out.println(cmd + " " + line);
        }
    }
}
