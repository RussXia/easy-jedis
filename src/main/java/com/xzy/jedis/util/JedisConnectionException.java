package com.xzy.jedis.util;

/**
 * @author xiazhengyue
 * @since 2021-03-01
 */
public class JedisConnectionException extends JedisException {
    private static final long serialVersionUID = -2852896443266515552L;

    public JedisConnectionException(String message) {
        super(message);
    }

    public JedisConnectionException(Throwable cause) {
        super(cause);
    }

    public JedisConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

}
