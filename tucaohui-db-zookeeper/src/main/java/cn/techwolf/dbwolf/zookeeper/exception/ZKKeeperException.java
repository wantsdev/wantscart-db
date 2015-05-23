package cn.techwolf.dbwolf.zookeeper.exception;

/**
 * zookeeper异常.
 * 
 */
public class ZKKeeperException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ZKKeeperException(final Throwable cause) {
        super();
        initCause(cause);
    }

}
