package com.xzy.jedis;

import com.xzy.jedis.util.JedisException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class Protocol {
    public static final String DOLLAR = "$";
    public static final String ASTERISK = "*";
    public static final String PLUS = "+";
    public static final String MINUS = "-";
    public static final String COLON = ":";
    public static final String COMMAND_DELIMITER = "\r\n";

    public static final byte DOLLAR_BYTE = DOLLAR.getBytes()[0];
    public static final byte ASTERISK_BYTE = ASTERISK.getBytes()[0];
    public static final byte MINUS_BYTE = MINUS.getBytes()[0];
    public static final byte COLON_BYTE = COLON.getBytes()[0];

    public void sendCommand(OutputStream os, String name, String... args) {
        StringBuilder builder = new StringBuilder();
        builder.append(ASTERISK).append(args.length + 1).append(
                COMMAND_DELIMITER);
        builder.append(DOLLAR).append(name.length()).append(COMMAND_DELIMITER);
        builder.append(name).append(COMMAND_DELIMITER);
        for (String arg : args) {
            builder.append(DOLLAR).append(arg.length()).append(
                    COMMAND_DELIMITER).append(arg).append(COMMAND_DELIMITER);
        }
        try {
            os.write(builder.toString().getBytes());
        } catch (IOException e) {
            // TODO don't know what to do here!
        }
    }

    public void processError(InputStream is) throws JedisException {
        String message = readLine(is);
        throw new JedisException(message);
    }

    private String readLine(InputStream is) {
        byte b;
        StringBuilder sb = new StringBuilder();

        try {
            while ((b = (byte) is.read()) != -1) {
                if (b == '\r') {
                    b = (byte) is.read();
                    if (b == '\n') {
                        break;
                    }
                }
                sb.append((char) b);
            }
        } catch (IOException e) {
            // TODO Dont know what to do here!
        }
        return sb.toString();
    }

    public String getBulkReply(InputStream is) throws JedisException {
        Object reply = process(is);
        return (String) reply;
    }

    public String getStatusCodeReply(InputStream is) throws JedisException {
        Object reply = process(is);
        return (String) reply;
    }

    private Object process(InputStream is) throws JedisException {
        try {
            byte b = (byte) is.read();
            if (b == MINUS_BYTE) {
                processError(is);
            } else if (b == ASTERISK_BYTE) {
                return processMultiBulkReply(is);
            } else if (b == COLON_BYTE) {
                return processInteger(is);
            } else if (b == DOLLAR_BYTE) {
                return processBulkReply(is);
            } else {
                return processStatusCodeReply(is);
            }
        } catch (IOException e) {
            // TODO check what to do here
            throw new JedisException(e.getMessage());
        }
        return null;
    }

    private Object processStatusCodeReply(InputStream is) {
        String ret = null;
        ret = readLine(is);
        return ret;
    }

    private Object processBulkReply(InputStream is) throws IOException {
        String ret = null;
        int len = Integer.parseInt(readLine(is));
        if (len == -1) {
            return null;
        }
        byte[] read = new byte[len];
        is.read(read);
        // read 2 more bytes for the command delimiter
        is.read();
        is.read();

        ret = new String(read);
        return ret;
    }

    private Object processInteger(InputStream is) {
        int ret = 0;
        String num = readLine(is);
        ret = Integer.parseInt(num);
        return ret;
    }

    private Object processMultiBulkReply(InputStream is) throws JedisException {
        List<Object> ret = new ArrayList<Object>();
        int num = Integer.parseInt(readLine(is));
        for (int i = 0; i < num; i++) {
            ret.add(process(is));
        }
        return ret;
    }
}