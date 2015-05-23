package com.tucaohui.dbwolf.client;

import com.tucaohui.dbwolf.client.ds.StormDataSourcePool;
import com.tucaohui.dbwolf.client.ds.StormDsPoolFactory;
import cn.techwolf.dbwolf.xml.DbInstanceConfig;
import cn.techwolf.dbwolf.xml.DbInstanceConfigDeserializer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;

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
public final class FileDbAgent implements DbAgent {

    //======================== static ===========================//

    private static final Log log = LogFactory.getLog(FileDbAgent.class);

    public ConnectionManager getConnectionManager() throws DbAgentInitException {
        return connectionManager;
    }

    //==================== instance field =========================//
    /**
     * 连接管理器，与dbagent一一对应.
     */
    private final ConnectionManager connectionManager;


    /**
     * 数据源缓存.
     */
    private final ConcurrentHashMap<String, StormDataSourcePool> dsPool = new ConcurrentHashMap<String, StormDataSourcePool>(); //


    private String path;

    //==================== constructor =========================//
    /**
     * 创建一个实例.
     */
    public FileDbAgent(String filepath) {
        this.connectionManager = new ConnectionManager(this);
        path = filepath;
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
     * 检索数据库配置.
     *
     * @param db
     * @return
     * @throws DbNotDefinedException 数据未定义或定义不完整
     */
    private DbInstanceConfig retriveDbConfig(final String db) throws DbNotDefinedException {
        DbInstanceConfig config = null;
        try {
            ClassPathResource classPathResource = new ClassPathResource(path);
            int size = classPathResource.getInputStream().available();
            byte[] dataBytes = new byte[size];

            classPathResource.getInputStream().read(dataBytes);

            config = new DbInstanceConfigDeserializer().deserialize(dataBytes);
            if (config == null) throw new DbNotDefinedException(db);
        } catch (Exception e) {
            throw new DbNotDefinedException(db, e);
        }
        return config;
    }

}
