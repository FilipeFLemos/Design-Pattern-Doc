package utils;

import java.io.*;
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

    public static synchronized void saveObject(String filename, Object object) {
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            ObjectOutputStream os = new ObjectOutputStream(fos);

            os.writeObject(object);

            os.close();
            fos.close();
        } catch (IOException ignored) {
        }
    }

    public static synchronized Object loadObject(String filename) {
        Object object = null;
        try {
            FileInputStream fis = new FileInputStream(filename);
            ObjectInputStream is = new ObjectInputStream(fis);

            object = is.readObject();

            is.close();
            fis.close();
        } catch (IOException | ClassNotFoundException ignored) {
        }

        return object;
    }
}
