package utils;


import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import models.DesignPattern;
import models.PatternInstance;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Utils {

    public static final int MIN_PATTERN_PARTICIPANTS_IN_COMMON = 2;
    public static final int PATTERN_DETECTION_DELAY = 30;

    public static String generatePatternInstanceId(ConcurrentHashMap<String, PatternInstance> patternInstanceById) {
        String id;
        do {
            id = Utils.generateAlphaNumericString();
        } while (patternInstanceById.containsKey(id));

        return id;
    }

    private static String generateAlphaNumericString() {
        int leftLimit = 48;
        int rightLimit = 122;
        int targetLength = 10;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        return generatedString;
    }

    public static JBLabel getFieldLabel(String text) {
        JBLabel jLabel = new JBLabel(text);
        jLabel.setComponentStyle(UIUtil.ComponentStyle.SMALL);
        jLabel.setFontColor(UIUtil.FontColor.BRIGHTER);
        jLabel.setBorder(JBUI.Borders.empty(0, 5, 2, 0));
        return jLabel;
    }

    public static Collection<VirtualFile> getVirtualFilesInProject(Project project) {
        Application application = ApplicationManager.getApplication();
        return application.runReadAction((Computable<Collection<VirtualFile>>) () -> {
            FileBasedIndex fileBasedIndex = FileBasedIndex.getInstance();
            return fileBasedIndex.getContainingFiles(FileTypeIndex.NAME, JavaFileType.INSTANCE, GlobalSearchScope.projectScope(project));
        });
    }

    public static ArrayList<String> getPatternFileNames() {
        ArrayList<String> patternFiles = new ArrayList<>();
        try {
            String[] fileNames = getResourceListing(Utils.class, "patterns/");
            patternFiles.addAll(Arrays.asList(fileNames));
        } catch (Exception ignored) {

        }
        return patternFiles;
    }

    public static Set<DesignPattern> getSupportedDesignPatterns() {
        Set<DesignPattern> designPatterns = new HashSet<>();
        try {
            String[] fileNames = getResourceListing(Utils.class, "patterns/");
            for (String fileName : fileNames) {
                ArrayList<String> fileLines = new ArrayList<>();
                InputStream inputStream = Utils.class.getClassLoader().getResourceAsStream("/patterns/" + fileName);
                InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(streamReader);
                for (String line; (line = reader.readLine()) != null; ) {
                    if (line.equals("End_Members")) {
                        break;
                    }
                    fileLines.add(line);
                }
                DesignPattern designPattern = createDesignPatternFromFileLines(fileLines);
                designPatterns.add(designPattern);
                inputStream.close();
                streamReader.close();
                reader.close();
            }
        } catch (Exception ignored) {

        }
        return designPatterns;
    }

    /**
     * List directory contents for a resource folder. Not recursive.
     * This is basically a brute-force implementation.
     * Works for regular files and also JARs.
     *
     * @param clazz Any java class that lives in the same place as the resources you want.
     * @param path  Should end with "/", but not start with one.
     * @return Just the name of each member item, not the full paths.
     * @throws URISyntaxException
     * @throws IOException
     * @author Greg Briggs
     */
    private static String[] getResourceListing(Class clazz, String path) throws URISyntaxException, IOException {
        URL dirURL = clazz.getClassLoader().getResource(path);
        if (dirURL != null && dirURL.getProtocol().equals("file")) {
            return new File(dirURL.toURI()).list();
        }

        if (dirURL == null) {
            /*
             * In case of a jar file, we can't actually find a directory.
             * Have to assume the same jar as clazz.
             */
            String me = clazz.getName().replace(".", "/") + ".class";
            dirURL = clazz.getClassLoader().getResource(me);
        }

        if (dirURL.getProtocol().equals("jar")) {
            String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!"));
            JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
            Enumeration<JarEntry> entries = jar.entries();
            Set<String> result = new HashSet<>();
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.startsWith(path) && !name.equals(path)) {
                    String entry = name.substring(path.length());
                    result.add(entry);
                }
            }
            return result.toArray(new String[0]);
        }

        throw new UnsupportedOperationException("Cannot list files for URL " + dirURL);
    }

    private static DesignPattern createDesignPatternFromFileLines(ArrayList<String> fileLines) {
        String name = fileLines.get(0);
        Set<String> roles = new HashSet<>();
        for (int i = 1; i < fileLines.size(); i++) {
            String line = fileLines.get(i);
            String[] stringSplit = line.split(" ");
            StringBuilder role = new StringBuilder();
            for (int j = 2; j < stringSplit.length; j++) {
                role.append(stringSplit[j]);
                if (j != stringSplit.length - 1) {
                    role.append(" ");
                }
            }
            roles.add(role.toString());
        }
        return new DesignPattern(name, roles);
    }
}
