package com.wsb.demoserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class IOUtil {

    public static byte[] readMessage(DataInputStream stream) throws IOException {
        int length = stream.readInt();
        byte[] message = new byte[length];
        stream.readFully(message);
        return message;
    }

    public static void writeMessage(
            DataOutputStream stream, byte[] message) throws IOException {
        stream.writeInt(message.length);
        stream.write(message);
        stream.flush();
    }

}
