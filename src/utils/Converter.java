package utils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Converter {
    public static byte[] longToByteArray(long l) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeLong(l);
        dos.flush();
        return bos.toByteArray();
    }

    public static long byteArrayToLong(byte[] data) {
        return ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN).getLong();
    }
}
