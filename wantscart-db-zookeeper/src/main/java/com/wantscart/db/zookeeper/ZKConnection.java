package com.wantscart.db.zookeeper;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.wantscart.db.zookeeper.exception.ZKInitException;
import org.apache.zookeeper.AsyncCallback.VoidCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

/**
 * zookeeper连接. 维护于zookeeper服务器之间的真正连接，完成最终操作.
 * 
 */
public class ZKConnection {

    /**
     * 默认session时间.
     */
    private static final int DEFAULT_SESSION_TIMEOUT = 30000;

    private final String servers;

    private final int sessionTimeOut;

    private final Lock zkLock = new ReentrantLock();

    private ZooKeeper zk;

    /**
     * 创建连接实例.
     * 
     * @param servers 服务器地址列表
     */
    public ZKConnection(final String servers) {
        this.servers = servers;
        this.sessionTimeOut = DEFAULT_SESSION_TIMEOUT;
    }

    /**
     * 创建连接实例.
     * 
     * @param servers 服务器地址列表
     * @param sessionTimeOut 客户端超时时间
     */
    public ZKConnection(final String servers, final int sessionTimeOut) {
        this.servers = servers;
        this.sessionTimeOut = sessionTimeOut;
    }

    /**
     * 连接服务器.
     * 
     * @param watcher
     */
    public void connect(final Watcher watcher) {
        zkLock.lock();
        try {
            if (zk != null) {
                throw new IllegalStateException("zookeeper connection has been established yet！");
            }
            this.zk = new ZooKeeper(this.servers, this.sessionTimeOut, watcher);
        } catch (IOException e) {
            throw new ZKInitException(servers, e);
        } finally {
            zkLock.unlock();
        }
    }

    /**
     * 关闭与服务器的连接.
     */
    public void close() {
        zkLock.lock();
        try {
            if (this.zk != null) {
                this.zk.close();
                this.zk = null;
            }
        } catch (Exception e) {} finally {
            zkLock.unlock();
        }
    }

    /**
     * 读取数据. <br>
     * 该方法不能保证获取到的数据是最新的.如果对实时性有特别高的要求，请使用syncGetData方法.
     * 
     * @param path
     * @param watcher
     * @param stat
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    public byte[] getData(final String path, final Watcher watcher, final Stat stat)
            throws KeeperException, InterruptedException {
        return zk.getData(path, watcher, stat);
    }

    /**
     * 从服务器读取最新版本的数据. <br>
     * 注意：此方法会引起阻塞，且不可中断，使用前请慎重考虑.
     * 
     * @param path
     * @param watcher
     * @param stat
     * @return
     * @throws Throwable
     * @throws KeeperException
     * @throws InterruptedException
     */
    public <T> T submitSyncTask(final String path, final Callable<T> callback)
            throws KeeperException, InterruptedException {
        final ZKSyncFutureTask<T> sync = new ZKSyncFutureTask<T>(path, callback);
        try {
            sync.run();
            return sync.get();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof KeeperException) {
                throw (KeeperException) e.getCause();
            }
        }
        return null;
    }

    public class ZKSyncFutureTask<T> implements RunnableFuture<T> {

        public static final int YIELD_LOOP = 1000;

        public static final int SLEEP_LOOP = 5000;

        public static final long DEFAULT_SLEEP_INTERVAL = 1000;

        boolean sync = false;

        int count = 0;

        String syncPath;

        Callable<T> callback;

        T result;

        Throwable throwable;

        public ZKSyncFutureTask(final String syncPath, final Callable<T> task) {
            this.callback = task;
            this.syncPath = syncPath;
        }

        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        public boolean isCancelled() {
            return false;
        }

        public boolean isDone() {
            return result != null;
        }

        @SuppressWarnings("static-access")
        public T get() throws InterruptedException, ExecutionException {
            int c = ZKSyncFutureTask.this.count;
            while (true) {
                c++;
                if (throwable != null) {
                    throw new ExecutionException(throwable);
                }
                if (ZKSyncFutureTask.this.result != null) {
                    return ZKSyncFutureTask.this.result;
                }
                if (c >= YIELD_LOOP) {//count 小于YIELD_LOOP则进行自旋
                    if (c <= SLEEP_LOOP) {
                        Thread.currentThread().yield();//count 小于SLEEP_LOOP则进行短暂等待
                    } else {
                        Thread.currentThread().sleep(DEFAULT_SLEEP_INTERVAL);//count 超过SLEEP_LOOP则进行长时间等待，默认为每一秒重试一次
                    }
                }
            }

        }

        public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException,
                TimeoutException {
            throw new IllegalStateException("unsupported method");//this should be executed
        }

        public void run() {
            try {
                zk.sync(ZKSyncFutureTask.this.syncPath, new VoidCallback() {

                    public void processResult(int rc, String path, Object ctx) {
                        try {
                            ZKSyncFutureTask.this.result = callback.call();
                        } catch (Throwable th) {
                            ZKSyncFutureTask.this.throwable = th;
                        }
                    }
                }, null);
            } catch (Throwable th) {
                ZKSyncFutureTask.this.throwable = th;
            }
        }
    }

    /**
     * 删除znode.
     * 
     * @param path
     * @throws InterruptedException
     * @throws KeeperException
     */
    public void delete(final String path) throws InterruptedException, KeeperException {
        zk.delete(path, -1);
    }

    /**
     * 修改znode数据.
     * 
     * @param path
     * @param data
     * @param version -1表示不考虑服务端数据版本
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    public Stat setData(final String path, final byte[] data, final int version)
            throws KeeperException, InterruptedException {
        return zk.setData(path, data, version);
    }

    /**
     * 创建znode.
     * 
     * @param path
     * @param data
     * @param persistent
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    public String create(final String path, final byte[] data, final CreateMode persistent)
            throws KeeperException, InterruptedException {
        return zk.create(path, data, Ids.OPEN_ACL_UNSAFE, persistent);
    }

    /**
     * 读取znode状态.
     * 
     * @param path
     * @param watch
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    public Stat exist(final String path, final boolean watch) throws KeeperException,
            InterruptedException {
        return zk.exists(path, watch);
    }

    /**
     * 读取child列表.
     * 
     * @param path
     * @param watch
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    public List<String> getChildren(final String path, final boolean watch) throws KeeperException,
            InterruptedException {
        return zk.getChildren(path, watch);
    }
}
