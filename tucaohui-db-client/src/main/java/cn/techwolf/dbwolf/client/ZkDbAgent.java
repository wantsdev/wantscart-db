package cn.techwolf.dbwolf.client;

import cn.techwolf.dbwolf.client.config.DbWolfConfig;
import cn.techwolf.dbwolf.client.ds.StormDataSourcePool;
import cn.techwolf.dbwolf.client.ds.StormDsPoolFactory;
import cn.techwolf.dbwolf.client.util.IPAddress;
import cn.techwolf.dbwolf.xml.DbInstanceConfig;
import cn.techwolf.dbwolf.xml.DbInstanceConfigDeserializer;
import cn.techwolf.dbwolf.zookeeper.DataListener;
import cn.techwolf.dbwolf.zookeeper.DataSerializer;
import cn.techwolf.dbwolf.zookeeper.ZKClient;
import cn.techwolf.dbwolf.zookeeper.exception.ZKDataSerializeException;
import cn.techwolf.dbwolf.zookeeper.exception.ZKKeeperException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.KeeperException;

import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据库配置代理，为每个数据库维护一个jdbc数据源. <br>
 * 每一个DbAgent实例都与一个zookeeper服务一一对应，此zookeeper服务上维护了数据库配置信息. <br>
 * 为保证安全性，该唯一实例对用户是不可见的，使用者不能通过此实例来获取配置信息. <br>
 * <strong>如果希望获取数据库连接，请使用<code>getConnectionManager()</code>
 * 方法来获取连接管理器.</strong>
 * 
 * 2014-03-31 modify by liujun 去除单例，使用IOC容器实现单例
 * 
 * 
 */
public final class ZkDbAgent implements DbAgent {

    //======================== static ===========================//

    private static final Log log = LogFactory.getLog(ZkDbAgent.class);

    private DbWolfConfig dbWolfConfig;

    public ConnectionManager getConnectionManager() throws DbAgentInitException {
        return connectionManager;
    }

    /**
     * 更新任务执行周期.
     */
    public static final long UPDATE_INTERVAL = 100 * 1000;

    /**
     * 更新任务初始延迟.
     */
    public static final long UPDATE_DELAY = 1000;

    //==================== instance field =========================//
    /**
     * 连接管理器，与dbagent一一对应.
     */
    private final ConnectionManager connectionManager;

    /**
     * 与zookeeper的连接.
     */
    private final ZKClient zkClient;

    /**
     * 数据源缓存.
     */
    private final ConcurrentHashMap<String, StormDataSourcePool> dsPool = new ConcurrentHashMap<String, StormDataSourcePool>(); //

    /**
     * 客户端定时刷新计时器.
     */
    private final Timer checkUpdate = new Timer(true);

    //==================== constructor =========================//
    /**
     * 创建一个实例.
     */
    public ZkDbAgent(DbWolfConfig dbWolfConfig) {
        this.connectionManager = new ConnectionManager(this);
        this.dbWolfConfig = dbWolfConfig;
        String endpoints = dbWolfConfig.getEndpoints();
        log.info("db agent connect to zk-sever " + endpoints);
        this.zkClient = new ZKClient(endpoints);
        checkUpdate.schedule(new UpdateTask(), UPDATE_DELAY, UPDATE_INTERVAL);
    }

    //===================== public =========================//

    /**
     * 获取数据源.
     * 
     * @param db 数据服务名称
     * @return jdbc数据源池
     */
    // 严重bug和问题: (fixed)
    // 1.没有与其他调用comparerAndReloadStormDs方法的地点形成同步
    // 2.没有取到ds的情况下是应当等待？还是应当直接报异常？
    // 3.没有取到ds的情况下是否应当对服务端进行监听？
    public StormDataSourcePool getDsPool(final String db) {
        StormDataSourcePool ds = dsPool.get(db);//happens-before
        if (ds == null) {
            synchronized (this) {
                if (ds == null) {
                    registerClient(db);//step1.注册客户端，用于监控
                    watchDbConfig(db);//step2.监听数据库配置
                    log.debug("not found ds for db " + db + " , retirve");
                    compareAndReload(retriveDbConfig(db));//step33.更新数据源
                }
            }
        }//double-check
        return dsPool.get(db);
    }

    /**
     * 重载数据库配置.
     * 
     * @param config
     */
    private synchronized void compareAndReload(final DbInstanceConfig config) {
        log.debug("reload db config " + config.toString());
        StormDataSourcePool old = dsPool.get(config.getName());
        if (old == null) {
            StormDataSourcePool ds = StormDsPoolFactory.createStormDs(this, config);
            dsPool.put(config.getName(), ds);
            return;
        }
        if (old.getTimeStamp() < config.getTimestamp()) {
            StormDataSourcePool ds = StormDsPoolFactory.createStormDs(this, config);
            dsPool.put(config.getName(), ds);
            if (old != null) old.close();
        }
    }

    /**
     * 定时更新配置任务.
     * 
     * @author huaiyu.du@opi-corp.com 2012-1-30 下午6:54:05
     */
    private class UpdateTask extends TimerTask {

        private final Log updateLog = LogFactory.getLog(UpdateTask.class);

        @Override
        public void run() {
            try {
                for (String db : dsPool.keySet()) {
                    updateLog.debug("execute update for " + db);
                    compareAndReload(retriveDbConfig(db));
                }
            } catch (Throwable e) {//使用timer必须catch住所有异常
                updateLog.error("an error occured when flush db config", e);
            }
        }
    }

    //================== private ====================//

    /**
     * 在服务端注册客户端信息.
     * 
     * @param db
     */
    private void registerClient(final String db) {
        final String path = cacuDbPath(db);
        final String apppath = path + "/apppoints";
        final String endpoint = apppath + "/" + IPAddress.getLocalAddress();
        try {
            zkClient.createPersistent(apppath, null, null, false);
        } catch (ZKKeeperException e) {
            if (!(e.getCause() instanceof KeeperException.NodeExistsException)) {
                throw e;
            }
        }
        try {
            zkClient.createEphemeral(endpoint, IPAddress.getLocalAddress(),
                    new DataSerializer<String>() {

                        public byte[] serialize(String obj) {
                            byte[] data = null;;
                            try {
                                data = obj.getBytes("utf-8");
                            } catch (UnsupportedEncodingException e) {
                                throw new ZKDataSerializeException(e);
                            }
                            return data;
                        }
                    });
        } catch (ZKKeeperException e) {
            if (!(e.getCause() instanceof KeeperException.NodeExistsException)) {
                throw e;
            }
        }
    }

    /**
     * 监听数据库配置.
     * 
     * @param db
     */
    private void watchDbConfig(final String db) {
        final String path = cacuDbPath(db);
        log.debug("watch for db " + db);
        zkClient.registerDataListener(new DataListener() {

            public String getPath() {
                return path;
            }

            public void onDataChange() {
                try {
                    DbInstanceConfig config = zkClient.syncReadData(path,
                            new DbInstanceConfigDeserializer());
                    log.debug("receive db config changed event for " + config.getName());
                    compareAndReload(config);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
    }

    /**
     * 根据db名称构造zookeeper path.
     * 
     * @param db
     * @return
     */
    private String cacuDbPath(final String db) {
        return dbWolfConfig.getZkRoot() + "/" + db;
    }

    /**
     * 检索数据库配置.
     * 
     * @param db
     * @return
     * @throws DbNotDefinedException 数据未定义或定义不完整
     */
    private DbInstanceConfig retriveDbConfig(final String db) throws DbNotDefinedException {
        DbInstanceConfig config = null;
        try {
            config = zkClient.syncReadData(cacuDbPath(db), new DbInstanceConfigDeserializer());
            if (config == null) throw new DbNotDefinedException(db);
        } catch (Exception e) {
            throw new DbNotDefinedException(db, e);
        }
        return config;
    }

}
