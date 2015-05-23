package com.tucaohui.dbwolf.zookeeper.exception;

/**
 * 对象反序列化异常.
 * 
 */
public class ZKDataDeserializeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ZKDataDeserializeException(final Throwable cause) {
        super();
        initCause(cause);
    }

}
