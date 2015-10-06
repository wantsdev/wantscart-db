package com.wantscart.db.zookeeper.exception;

/**
 * 初始化异常，主要针对Zookeeper启动时的错误.
 * 
 */
public class ZKInitException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ZKInitException(final String servers) {
        super("Unable to connect to [" + servers + "]");
    }

    public ZKInitException(final Throwable cause) {
        super();
        initCause(cause);
    }

    public ZKInitException(final String servers, final Throwable cause) {
        super("Unable to connect to [" + servers + "]");
        initCause(cause);
    }

}
