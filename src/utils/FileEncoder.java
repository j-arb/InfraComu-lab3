package utils;

import java.util.Base64;

public class FileEncoder {
    
    public static String encodeFile(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    public static byte[] decodeFile(String data) {
        return Base64.getDecoder().decode(data);
    }
    
}
