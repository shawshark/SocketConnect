package net.shawshark.socketconnect.objects;

import io.netty.util.CharsetUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Utils {
    public static Object serializableFromString( String s ) throws IOException,
            ClassNotFoundException {
        byte [] data = Base64.getDecoder().decode( s.getBytes(StandardCharsets.UTF_8) );
        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(  data ) );
        Object o  = ois.readObject();
        ois.close();
        return o;
    }


    /** Write the object to a Base64 string. */
    public static String serializableToString( Serializable o ) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( o );
        oos.close();

        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    public static byte[] toByte(String s) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeUTF(s);
        oos.close();

        return baos.toByteArray();
    }

    public static String fromByte(byte[] b) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(  b ) );
        String o  = ois.readUTF();
        ois.close();
        return o;
    }
}
