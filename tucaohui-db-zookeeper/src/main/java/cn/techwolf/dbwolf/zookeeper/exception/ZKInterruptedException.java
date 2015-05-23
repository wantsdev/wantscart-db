package cn.techwolf.dbwolf.zookeeper.exception;

/**
 * 针对zookeeper调用中发生的中断，以及经过包装后引入的可终端阻塞可能会引发的异常.
 * 
 */
public class ZKInterruptedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ZKInterruptedException(final InterruptedException cause) {
        super(cause);
        Thread.currentThread().interrupt();//恢复中断状态
    }

}
