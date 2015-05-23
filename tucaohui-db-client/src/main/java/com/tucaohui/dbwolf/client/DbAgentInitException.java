package com.tucaohui.dbwolf.client;

/**
 * DbAgent初始化异常.
 * 
 */
public class DbAgentInitException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DbAgentInitException(final String message) {
        super(message);
    }

    public DbAgentInitException(final Throwable cause) {
        super();
        initCause(cause);
    }

    public DbAgentInitException(final String message, final Throwable cause) {
        super(message);
        initCause(cause);
    }

}
