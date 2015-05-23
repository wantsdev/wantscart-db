package com.tucaohui.dbwolf.client.ds;

public class NoReadableDsDefineException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NoReadableDsDefineException(final String dbName) {
        super("[" + dbName + "] didn't contain a readable datasource");
    }

    public NoReadableDsDefineException(final Throwable cause) {
        super();
        initCause(cause);
    }

}
