package com.tucaohui.dbwolf.client;

/**
 * 数据库未定义异常.
 * 
 */
public class DbNotDefinedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DbNotDefinedException(final String db) {
        super("config for [" + db + "] is not be defined properly");
    }

    public DbNotDefinedException(final String db, final Throwable cause) {
        super("config for [" + db + "] is not be defined properly");
        initCause(cause);
    }

}
