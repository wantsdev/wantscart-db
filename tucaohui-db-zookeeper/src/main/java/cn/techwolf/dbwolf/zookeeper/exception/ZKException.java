package cn.techwolf.dbwolf.zookeeper.exception;

public class ZKException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ZKException(final String message) {
        super(message);
    }

    public ZKException(final Throwable cause) {
        super();
        initCause(cause);
    }

    public ZKException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }

}
