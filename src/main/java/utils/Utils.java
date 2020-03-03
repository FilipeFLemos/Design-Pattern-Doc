package utils;


import java.util.Random;

public class Utils {

    public static String generateAlphaNumericString(){
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
