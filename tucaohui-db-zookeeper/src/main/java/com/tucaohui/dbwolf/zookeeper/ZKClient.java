package com.tucaohui.dbwolf.zookeeper;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

import com.tucaohui.dbwolf.zookeeper.exception.ZKTimeoutException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.ConnectionLossException;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.KeeperException.SessionExpiredException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.data.Stat;

import com.tucaohui.dbwolf.zookeeper.exception.ZKException;
import com.tucaohui.dbwolf.zookeeper.exception.ZKInterruptedException;
import com.tucaohui.dbwolf.zookeeper.exception.ZKKeeperException;

/**
 * zookeeper客户端，封装zookeeper的多数操作. <br>
 * <ol>
 * <li>用listener的概念取代了watcher，解决了同一客户端对节点进行多次watch只能收到一次通知的限制.</li>
 * <li>保证客户端的高可用性，封装了在连接断开或session超时等情况下的等待和重连过程.</li>
 * <li>封装了zookeeper的异常，将部分的checked异常重新包装为runtime异常.</li>
 * <li>提供了一个同步版本的syncReadData方法，保证数据的实时性.</li>
 * </ol>
 * 
 * 
 */
public class ZKClient implements Watcher {

    private static final Log log = LogFactory.getLog(ZKClient.class);

    /**
     * 默认超时时间，以millsecond为单位.
     */
    public static final int DEFAULT_CONNECTION_TIMEOUT = 10000;

    /**
     * zookeeper连接.
     */
    private ZKConnection connection;

    /**
     * 服务器端点列表.
     */
    private final String zkServers;

    /**
     * 连接zookeeper超时时间.
     */
    private final long connectionTimeout;

    /**
     * 事件分发线程.
     */
    private final EventThread eventThread;

    /**
     * 事件处理锁.
     */
    private final ZKLock eventLock = new ZKLock();

    /**
     * 监听客户端状态的listener.
     */
    private final Set<StateListener> stateListeners = new HashSet<StateListener>();

    /**
     * 监听client节点的listener.
     */
    private final ConcurrentHashMap<String, Set<DataListener>> dataListeners = new ConcurrentHashMap<String, Set<DataListener>>();

    /**
     * 监听db配置信息的listener.
     */
    private final ConcurrentHashMap<String, Set<NodeListener>> nodeListeners = new ConcurrentHashMap<String, Set<NodeListener>>();

    /**
     * 客户端关闭标记.
     */
    private volatile boolean shutdownTriggered;

    /**
     * 客户端当前的状态，比较重要的是syncconnect和expired两个状态.
     */
    private volatile KeeperState currentState;

    /**
     * 当前进入process方法的线程标记.
     */
    private volatile Thread zookeeperEventThread;

    /**
     * 工具类
     */
    private final ZKHelper helper = new ZKHelper();

    //====================构造函数=========================//
    /**
     * 创建一个客户端实例.
     * 
     * @param zkServers
     *        服务器端点地址,比如：10.6.39.74:2181,10.6.39.80:2181,10.6.33.105:2181.
     *        但zkServers不能包含任何chroot路径
     *        .去掉chroot路径是因为在zookeeper中还不能真正做到对chroot透明，很难统一api.
     * 
     * @throws com.tucaohui.dbwolf.zookeeper.exception.ZKTimeoutException 当链接等待时间超过
     *         <code>DEFAULT_CONNECTION_TIMEOUT</code>后
     */
    public ZKClient(final String zkServers) {
        this.zkServers = zkServers;
        this.connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
        connection = new ZKConnection(zkServers);
        eventThread = new EventThread(zkServers);
        connect(connectionTimeout, this);
    }

    /**
     * 创建一个客户端实例.
     * 
     * @param zkServers
     *        服务器端点地址,比如：10.6.39.74:2181,10.6.39.80:2181,10.6.33.105:2181.
     * @param sessionTimeout
     *        会话超时时间，客户端断开超过此时间后，服务端将会清除掉所有的相关信息，包括watcher和临时节点
     */
    public ZKClient(final String zkServers, final int sessionTimeout) {
        this.zkServers = zkServers;
        this.connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
        connection = new ZKConnection(zkServers, sessionTimeout);
        eventThread = new EventThread(zkServers);
        connect(connectionTimeout, this);
    }

    /**
     * 创建一个客户端实例.
     * 
     * @param zkServers
     *        服务器端点地址,比如：10.6.39.74:2181,10.6.39.80:2181,10.6.33.105:2181.
     * @param sessionTimeout 会话超时时间
     * @param connectionTimeout 连接超时时间
     * 
     * @throws com.tucaohui.dbwolf.zookeeper.exception.ZKTimeoutException 当链接等待时间超过connectionTimeout后
     */
    public ZKClient(final String zkServers, final int sessionTimeout, final long connectionTimeout) {
        this.zkServers = zkServers;
        this.connectionTimeout = connectionTimeout;
        connection = new ZKConnection(zkServers, sessionTimeout);
        eventThread = new EventThread(zkServers);
        connect(connectionTimeout, this);
    }

    //====================public=========================//
    /**
     * 获取一个znode的数据.
     * 
     * @param path znode路径
     * @param serlizer 反序列化器
     * @return 经过反序列化的数据值
     * @throws ZKKeeperException zookeeper发生异常时
     * @throws ZKInterruptedException 调用zookeeper服务时，线程发生中断
     * @throws ZKException
     */
    public <T> T readData(final String path, final DataDeserializer<T> serlizer) {
        final Stat stat = new Stat();
        try {
            byte[] data = retryUntilConnected(new Callable<byte[]>() {

                public byte[] call() throws Exception {
                    return connection.getData(path, null, stat);
                }
            });
            return serlizer.deserialize(data);
        } catch (KeeperException e) {
            throw new ZKKeeperException(e);
        } catch (InterruptedException e) {
            throw new ZKInterruptedException(e);
        } catch (Exception e) {
            throw new ZKException(e);
        }
    }

    /**
     * 强行要求服务端同步，随后获取一个znode的数据.
     * 
     * @param path znode路径
     * @param serlizer 序列化器
     * @return 经过反序列化的数据值
     */
    public <T> T syncReadData(final String path, final DataDeserializer<T> serlizer) {
        final Stat stat = new Stat();
        try {
            byte[] data = retryUntilConnected(new Callable<byte[]>() {

                public byte[] call() throws Exception {
                    return connection.submitSyncTask(path, new Callable<byte[]>() {

                        public byte[] call() throws Exception {
                            return connection.getData(path, null, stat);
                        }

                    });
                }
            });
            return serlizer.deserialize(data);
        } catch (KeeperException e) {
            throw new ZKKeeperException(e);
        } catch (InterruptedException e) {
            throw new ZKInterruptedException(e);
        } catch (Exception e) {
            throw new ZKException(e);
        }
    }

    /**
     * 修改一个znode的数据.
     * 
     * @param path znode路径
     * @param obj 待写入的数据对象
     * @param serializer 对象序列化器实例
     */
    public <T> void writeData(final String path, final T obj, final DataSerializer<T> serializer) {
        writeData(path, obj, serializer, -1);
    }

    /**
     * 修改一个node数据.
     * 
     * @param path znode路径
     * @param obj 待写入的数据对象
     * @param serializer 对象序列化器实例
     * @param expectedVersion -1表示不限定版本
     * @throws ZKKeeperException zookeeper发生异常时
     * @throws ZKInterruptedException 调用zookeeper服务时，线程发生中断
     * @throws ZKException
     */
    public <T> void writeData(final String path, final T obj, final DataSerializer<T> serializer,
            final int expectedVersion) {
        try {
            retryUntilConnected(new Callable<Stat>() {

                public Stat call() throws Exception {
                    return connection.setData(path, serializer.serialize(obj), expectedVersion);
                }
            });

        } catch (KeeperException e) {
            throw new ZKKeeperException(e);
        } catch (InterruptedException e) {
            throw new ZKInterruptedException(e);
        } catch (Exception e) {
            throw new ZKException(e);
        }
    }

    /**
     * 判断一个znode是否存在.
     * 
     * @param path znode路径
     * @return znode状态
     * @throws ZKKeeperException zookeeper发生异常时
     * @throws ZKInterruptedException 调用zookeeper服务时，线程发生中断
     * @throws ZKException
     */
    public Stat exist(final String path) {
        try {
            return helper.exist(path, hasDataListener(path));
        } catch (KeeperException e) {
            throw new ZKKeeperException(e);
        } catch (InterruptedException e) {
            throw new ZKInterruptedException(e);
        } catch (Exception e) {
            throw new ZKException(e);
        }
    }

    /**
     * 获取某个znode的全部子节点.
     * 
     * @param path znode路径
     * @return 子节点列表
     * 
     * @throws ZKKeeperException zookeeper发生异常时
     * @throws ZKInterruptedException 调用zookeeper服务时，线程发生中断
     * @throws ZKException
     */
    public List<String> getChildren(final String path) {
        try {
            return helper.getChildren(path, hasNodeListener(path));
        } catch (KeeperException e) {
            throw new ZKKeeperException(e);
        } catch (InterruptedException e) {
            throw new ZKInterruptedException(e);
        } catch (Exception e) {
            throw new ZKException(e);
        }
    }

    /**
     * 删除节点.
     * 
     * @param path znode路径
     * 
     * @throws ZKKeeperException zookeeper发生异常时
     * @throws ZKInterruptedException 调用zookeeper服务时，线程发生中断
     * @throws ZKException
     */
    public void deleteNode(final String path) {
        try {
            retryUntilConnected(new Callable<Object>() {

                public Object call() throws Exception {
                    connection.delete(path);
                    return null;
                }
            });
        } catch (KeeperException e) {
            throw new ZKKeeperException(e);
        } catch (InterruptedException e) {
            throw new ZKInterruptedException(e);
        } catch (Exception e) {
            throw new ZKException(e);
        }
    }

    /**
     * 创建一个持久节点.
     * 
     * @param path znode路径
     * @param obj
     * @param serializer
     * @param createParents true则创建父节点，false则不创建
     * 
     * @throws ZKKeeperException zookeeper发生异常时
     * @throws ZKInterruptedException 调用zookeeper服务时，线程发生中断
     * @throws ZKException
     */
    public <T> void createPersistent(final String path, final T obj,
            final DataSerializer<T> serializer, final boolean createParents) {
        if (obj != null && serializer == null) {
            throw new IllegalArgumentException("serializer can not be null");
        }
        try {
            helper.create(path, obj, serializer, CreateMode.PERSISTENT);
        } catch (NoNodeException e) {
            String parentDir = path.substring(0, path.lastIndexOf("/"));
            if (parentDir.length() > 0) {
                createPersistent(parentDir, null, null, createParents);
            }
            createPersistent(path, obj, serializer, createParents);
        } catch (KeeperException e) {
            throw new ZKKeeperException(e);
        } catch (InterruptedException e) {
            throw new ZKInterruptedException(e);
        } catch (Exception e) {
            throw new ZKException(e);
        }
    }

    /**
     * 创建一个临时节点.
     * 
     * @param path znode路径
     * @param obj
     * @param serializer
     * 
     * @throws ZKKeeperException zookeeper发生异常时
     * @throws ZKInterruptedException 调用zookeeper服务时，线程发生中断
     * @throws ZKException
     */
    public <T> void createEphemeral(final String path, final T obj,
            final DataSerializer<T> serializer) {
        try {
            helper.create(path, obj, serializer, CreateMode.EPHEMERAL);
        } catch (KeeperException e) {
            throw new ZKKeeperException(e);
        } catch (InterruptedException e) {
            throw new ZKInterruptedException(e);
        } catch (Exception e) {
            throw new ZKException(e);
        }
    }

    /**
     * 注册一个数据观察者.
     * 
     * @param watcher
     */
    public void registerDataListener(final DataListener listener) {
        final String path = listener.getPath();
        log.debug("watch for data " + path);
        synchronized (dataListeners) {
            Set<DataListener> listeners = dataListeners.get(path);
            if (listeners == null) {
                listeners = new CopyOnWriteArraySet<DataListener>();
                dataListeners.put(path, listeners);
            }
            listeners.add(listener);
            watchForData(path);
        }
    }

    public void unRegisterDataListener(final DataListener listener) {
        final String path = listener.getPath();
        log.debug("unwatch for data " + path);
        synchronized (dataListeners) {
            Set<DataListener> listeners = dataListeners.get(path);
            if (listeners != null) {
                listeners.remove(listener);
            }
            if (listeners == null || listeners.isEmpty()) {
                dataListeners.remove(path);
            }
        }
    }

    /**
     * 注册一个node观察者.
     * 
     * @param watcher
     */
    public void registerNodeListener(final NodeListener listener) {
        final String path = listener.getPath();
        log.debug("watch for node " + path);
        synchronized (nodeListeners) {
            Set<NodeListener> listeners = nodeListeners.get(path);
            if (listeners == null) {
                listeners = new CopyOnWriteArraySet<NodeListener>();
                nodeListeners.put(path, listeners);
            }
            listeners.add(listener);
        }
        watchForNode(path);
    }

    public void unRegisterNodeListener(final NodeListener listener) {
        final String path = listener.getPath();
        log.debug("unwatch for node " + path);
        synchronized (nodeListeners) {
            Set<NodeListener> listeners = nodeListeners.get(path);
            if (listeners != null) {
                listeners.remove(listener);
            }
            if (listeners == null || listeners.isEmpty()) {
                nodeListeners.remove(path);
            }
        }
    }

    /**
     * 不停地尝试执行任务，直到任务执行完毕.
     * 
     * @param callable
     * @return 执行任务返回的结果
     * @throws InterruptedException
     * @throws Exception
     */
    public <T> T retryUntilConnected(final Callable<T> callable) throws InterruptedException,
            Exception {
        Assertion.isNotInEventThread(zookeeperEventThread);//不能在在zookeeper的通知线程内调用
        while (true) {
            try {
                return callable.call();
            } catch (ConnectionLossException e) {
                log.debug("caught a ConnectionLossException  , do retry", e);
                Thread.yield();//暂缓一下,出现此异常后客户端可以按照zkservers列表进行切换，只需要等待即可
                waitUntilConnected(this.connectionTimeout, TimeUnit.MILLISECONDS);
            } catch (SessionExpiredException e) {
                log.debug("caught a SessionExpiredException  , do retry", e);
                Thread.yield();//暂缓一下，出现此异常往往是不可恢复的，需要重新创建连接，
                               //但是我们不在此处进行处理，而是交给process方法来完成
                waitUntilConnected(this.connectionTimeout, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                log.debug("caught a Exception  , do retry");
                throw e;
            }
        }
    }

    /**
     * zookeeper的回调，需要考虑到四种情况. 1.节点改变 2.数据改变 3.连接断开 4.session超时
     * 
     */
    public void process(final WatchedEvent event) {
        log.debug("receive event " + event.toString());
        zookeeperEventThread = Thread.currentThread();//一个标记，防止死锁

        boolean stateChanged = event.getPath() == null;
        boolean znodeChanged = event.getPath() != null;
        boolean dataChanged = event.getType() == EventType.NodeDataChanged
                || event.getType() == EventType.NodeDeleted
                || event.getType() == EventType.NodeCreated
                || event.getType() == EventType.NodeChildrenChanged;

        eventLock.lock();

        if (isShutdownTriggered()) {
            log.debug("ignoring event '{" + event.getType() + " | " + event.getPath()
                    + "}' since shutdown triggered");
            return;
        }

        try {
            if (stateChanged) {
                processStateChange(event);
            }
            if (dataChanged) {
                processDataOrChildChange(event);
            }
        } finally {
            if (stateChanged) {
                eventLock.getStateCondition().signalAll(); //唤醒当前所有等待连接成功的客户端

                //				if (event.getState() == KeeperState.Expired) {//超时后，需要进行重连，对于所有的监听者，应当通知他们可能有东西发生改变了
                //					eventLock.getZnodeCondition().signalAll();
                //					eventLock.getDataCondition().signalAll();
                //TODO 是否需要触发所有事件？
                //				}
            }

            if (znodeChanged) {
                eventLock.getZnodeCondition().signalAll();//唤醒当前所有等待节点创建和删除的客户端
            }
            if (dataChanged) {
                eventLock.getDataCondition().signalAll();//唤醒当前所有等待节点数据改变的客户端
            }
            eventLock.unlock();
        }
    }

    //====================private=========================//

    /**
     * 建立zookeeper连接
     * 
     * @param connectionTimeout 连接超时时间
     * @param watcher
     * @throws com.tucaohui.dbwolf.zookeeper.exception.ZKTimeoutException 当链接等待时间超过connectionTimeout后
     */
    private void connect(final long connectionTimeout, final Watcher watcher) {
        log.debug("connect to server");
        boolean started = false;
        try {
            eventLock.lockInterruptibly();
            setShutdownTrigger(false);
            eventThread.start();
            connection.connect(watcher);
            if (!waitUntilConnected(connectionTimeout, TimeUnit.MILLISECONDS)) {
                log.debug("wait to connect timeout " + connectionTimeout);
                throw new ZKTimeoutException(this.zkServers, connectionTimeout);
            }
            started = true;
        } catch (InterruptedException e) {
            throw new ZKInterruptedException(e);
        } finally {
            eventLock.unlock();
            if (!started) {
                close();
            }
        }
    }

    /**
     * Close the client.
     * 
     * @throws ZkInterruptedException
     */
    public void close() throws ZKInterruptedException {
        if (connection == null) {
            return;
        }
        log.debug("closing ZKClient...");
        eventLock.lock();
        try {
            setShutdownTrigger(true);
            eventThread.interrupt();
            eventThread.join(2000);
            connection.close();
            connection = null;
        } catch (InterruptedException e) {
            throw new ZKInterruptedException(e);
        } finally {
            eventLock.unlock();
        }
        log.debug("closing ZKClient...done");
    }

    /**
     * 在与zookeeper建立连接之前一直等待.
     * 
     * @param connectionTimeout 连接超时时间
     * @param unit 超时时间单位
     * @return
     * @throws InterruptedException 当阻塞被中断
     */
    private boolean waitUntilConnected(final long connectionTimeout, final TimeUnit unit)
            throws InterruptedException {
        return waitForKeeperState(KeeperState.SyncConnected, connectionTimeout, unit);
    }

    private boolean waitForKeeperState(final KeeperState exceptedState,
            final long connectionTimeout, final TimeUnit unit) throws InterruptedException {
        Assertion.isNotInEventThread(zookeeperEventThread);//不能在在zookeeper的通知线程内调用
        Date deadline = new Date(System.currentTimeMillis() + connectionTimeout);
        try {
            eventLock.lockInterruptibly();
            while (currentState != exceptedState) {
                return eventLock.getStateCondition().awaitUntil(deadline);//在达到指定时间之前阻塞
            }
            return true;
        } catch (InterruptedException e) {
            throw e;
        } finally {
            eventLock.unlock();
        }
    }

    /**
     * 处理节点数据或子节点的改变.
     * 
     * @param event
     */
    private void processDataOrChildChange(final WatchedEvent event) {
        String path = event.getPath();
        boolean dataChanged = event.getType() == EventType.NodeCreated
                || event.getType() == EventType.NodeDeleted
                || event.getType() == EventType.NodeDataChanged;
        if (dataChanged) {
            for (Set<DataListener> listeners : dataListeners.values()) {
                fireDataChangedEvent(path, listeners);
            }
        }
        boolean nodeChanged = event.getType() == EventType.NodeDeleted
                || event.getType() == EventType.NodeChildrenChanged
                || event.getType() == EventType.NodeCreated;
        if (nodeChanged) {
            for (Set<NodeListener> listeners : nodeListeners.values()) {
                fireNodeChangedEvent(path, listeners);
            }
        }
    }

    /**
     * 引发一个节点改变事件.
     * 
     * @param path znode路径
     * @param listeners 待通知的监听器
     */
    private void fireNodeChangedEvent(final String path, final Set<NodeListener> listeners) {
        for (final NodeListener lis : listeners) {
            eventThread.send(new ZKEvent() {

                @Override
                void run() throws Exception {
                    helper.exist(path, true);
                    helper.getChildren(path, true);
                    if (path.equals(lis.getPath())) {
                        lis.onNodeChange();
                    }
                }
            });
        }
    }

    /**
     * 引发一个数据改变事件.
     * 
     * @param path znode路径
     * @param listeners 待通知的监听器
     */
    private void fireDataChangedEvent(final String path, final Set<DataListener> listeners) {
        for (final DataListener lis : listeners) {
            eventThread.send(new ZKEvent() {

                @Override
                void run() throws Exception {
                    helper.exist(path, true);
                    if (path.equals(lis.getPath())) {
                        lis.onDataChange();
                    }
                }
            });
        }
    }

    /**
     * 状态发生变化进行回调.
     * 
     * @param event zookeeper事件
     */
    private void processStateChange(final WatchedEvent event) {
        setCurrentState(event.getState());
        if (KeeperState.Expired == currentState) {
            reconnect();//超时后状态无法恢复，必须进行重连
            fireNewSessionEvents();
        }
    }

    /**
     * 通知所有statelistener.
     * 
     */
    private void fireNewSessionEvents() {
        for (final StateListener listener : stateListeners) {
            eventThread.send(new ZKEvent() {

                @Override
                void run() {
                    listener.onNewSession();//必须应对新建立的连接
                }
            });
        }
    }

    /**
     * zookeeper重连.
     * 
     * @throws ZKInterruptedException 连接zookeeper服务时，线程发生中断
     */
    private void reconnect() {
        try {
            eventLock.lockInterruptibly();
            connection.close();
            connection.connect(this);
        } catch (InterruptedException e) {
            throw new ZKInterruptedException(e);
        } finally {
            eventLock.unlock();
        }
    }

    /**
     * 监视节点数据变化.
     * 
     * @param path znode路径
     * @throws ZKKeeperException zookeeper发生异常时
     * @throws ZKInterruptedException 调用zookeeper服务时，线程发生中断
     * @throws ZKException
     */
    private void watchForData(final String path) {
        try {
            retryUntilConnected(new Callable<Object>() {

                public Object call() throws Exception {
                    connection.exist(path, true);
                    return null;
                }

            });
        } catch (KeeperException e) {
            throw new ZKKeeperException(e);
        } catch (InterruptedException e) {
            throw new ZKInterruptedException(e);
        } catch (Exception e) {
            throw new ZKException(e);
        }
    }

    /**
     * 监视节点变化.
     * 
     * @param path znode路径
     * @throws ZKKeeperException zookeeper发生异常时
     * @throws ZKInterruptedException 调用zookeeper服务时，线程发生中断
     * @throws ZKException
     */
    private void watchForNode(final String path) {
        try {
            retryUntilConnected(new Callable<Object>() {

                public Object call() throws Exception {
                    helper.exist(path, true);
                    helper.getChildren(path, true);
                    return null;
                }
            });
        } catch (KeeperException e) {
            throw new ZKKeeperException(e);
        } catch (InterruptedException e) {
            throw new ZKInterruptedException(e);
        } catch (Exception e) {
            throw new ZKException(e);
        }
    }

    /**
     * 检查当前节点是否存在监听器.
     * 
     * @param path znode路径
     * @return true表示已经进行过监听,false表示未进行过监听
     */
    private boolean hasDataListener(final String path) {
        return dataListeners.containsKey(path) ? !dataListeners.get(path).isEmpty() : false;
    }

    /**
     * 检查子节点是否存在监听器.
     * 
     * @param path znode路径
     * @return true表示已经进行过监听,false表示未进行过监听
     */
    private boolean hasNodeListener(final String path) {
        return nodeListeners.contains(path) ? !nodeListeners.get(path).isEmpty() : false;
    }

    /**
     * 修改客户端的当前状态.
     * 
     * @param state
     */
    private void setCurrentState(final KeeperState state) {
        this.currentState = state;
    }

    public void setShutdownTrigger(boolean triggerState) {
        shutdownTriggered = triggerState;
    }

    /**
     * 客户端是否已经被关闭.
     * 
     * @return
     */
    public boolean isShutdownTriggered() {
        return shutdownTriggered;
    }

    /**
     * 一个工具类，简单地封装了一些方法.
     * 
     * @author huaiyu.du@opi-corp.com 2012-1-29 下午4:29:23
     */
    private final class ZKHelper {

        /**
         * 带有重试过程的getChildren.
         * 
         * @param path
         * @param watch
         * 
         * @throws KeeperException
         * @throws InterruptedException
         * @throws Exception
         */
        private List<String> getChildren(final String path, final boolean watch)
                throws InterruptedException, Exception {
            return retryUntilConnected(new Callable<List<String>>() {

                public List<String> call() throws Exception {
                    return connection.getChildren(path, watch);
                }
            });
        }

        /**
         * 带有重试过程的exist.
         * 
         * @param path
         * @param watch
         * @throws KeeperException
         * @throws InterruptedException
         * @throws Exception
         */
        private Stat exist(final String path, final boolean watch) throws InterruptedException,
                Exception {
            return retryUntilConnected(new Callable<Stat>() {

                public Stat call() throws Exception {
                    return connection.exist(path, watch);
                }
            });
        }

        /**
         * 创建节点.
         * 
         * @param path
         * @param obj
         * @param serializer
         * @param createMode
         * @throws KeeperException
         * @throws InterruptedException
         * @throws Exception
         */
        private <T> void create(final String path, final T obj, final DataSerializer<T> serializer,
                final CreateMode createMode) throws InterruptedException, Exception {
            retryUntilConnected(new Callable<String>() {

                public String call() throws Exception {
                    byte[] data = obj != null ? serializer.serialize(obj) : new byte[0];
                    return connection.create(path, data, createMode);
                }
            });
        }
    }

}
