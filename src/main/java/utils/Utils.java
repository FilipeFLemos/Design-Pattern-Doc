package utils;


import models.PatternInstance;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class Utils {

    public static final int MIN_PATTERN_PARTICIPANTS_IN_COMMON = 2;
    public static final int PATTERN_DETECTION_DELAY = 10;

    public static String generatePatternInstanceId(ConcurrentHashMap<String, PatternInstance> patternInstanceById) {
        String id;
        do {
            id = Utils.generateAlphaNumericString();
        } while (patternInstanceById.containsKey(id));

        return id;
    }

    private static String generateAlphaNumericString(){
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
}
