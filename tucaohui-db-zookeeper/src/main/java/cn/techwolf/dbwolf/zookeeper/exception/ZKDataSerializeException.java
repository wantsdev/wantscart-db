package cn.techwolf.dbwolf.zookeeper.exception;

/**
 * 对象序列化异常.
 * 
 */
public class ZKDataSerializeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ZKDataSerializeException(final Throwable cause) {
        super();
        initCause(cause);
    }
}
