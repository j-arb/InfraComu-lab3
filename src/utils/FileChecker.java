package utils;

import java.io.File;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;

public class FileChecker {

    /**
     * Generates MD5 hash of file. Returns hex string of hash.
     */
    public static String generateHash(File file) {
        try {
            byte[] data = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
            byte[] hashB = MessageDigest.getInstance("MD5").digest(data);
            return new BigInteger(1, hashB).toString(16);
        } catch(Exception e) {
            System.out.println("Unable to generate HASH.");
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Checks if hash matches with file's MD5 hash as hex string.
     * Returns true if its a match.
     */
    public static boolean checkHash(String hash, File file) {
        String hash2 = generateHash(file);
        return hash.equals(hash2);
    }
}
