package com.tucaohui.dbwolf.jade.datasource.configurator;

import com.tucaohui.dbwolf.client.config.DbWolfConfig;
import com.tucaohui.dbwolf.zookeeper.DataDeserializer;
import com.tucaohui.dbwolf.zookeeper.DataListener;
import com.tucaohui.dbwolf.zookeeper.ZKClient;
import com.tucaohui.dbwolf.zookeeper.exception.ZKDataDeserializeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author <a href="mailto:tao.zhang@renren-inc.com">Kylen Zhang</a>
 *         Initial created at 2012-10-24 下午03:34:30
 */
public class DbwolfMemZkXmlDocConfigurator extends DbwolfXmlDocConfigurator {

    private static final Log log = LogFactory.getLog(DbwolfMemZkXmlDocConfigurator.class);

    /**
     * 与zookeeper的连接.
     */
    private static ZKClient zkClient;

    private DbWolfConfig config;

    private String jadeConfigPath;

    public DbwolfMemZkXmlDocConfigurator() {
        this(null);
    }

    public DbwolfMemZkXmlDocConfigurator(DbWolfConfig config) {
        this.config = config == null ? DbWolfConfig.getConfig() : config;
        jadeConfigPath = this.config.getZkRoot() + "/jade-config";
        initJadeConfig();
    }

    /**
     * 
     * @return
     */
    private void retriveJadeConfig() {
        try {
            byte[] jadeConfig = zkClient.syncReadData(jadeConfigPath,
                    new DataDeserializer<byte[]>() {

                        public byte[] deserialize(final byte[] data) {
                            if (data == null || data.length == 0) {
                                return null;
                            }
                            try {
                                return data;
                            } catch (Exception e) {
                                throw new ZKDataDeserializeException(e);
                            }
                        }
                    });
            if (jadeConfig != null) {
                this.setJadeConfigContent(jadeConfig);
                if (logger.isDebugEnabled()) {
                    logger.debug("Got new jade-config content:" + new String(jadeConfig));
                }
            }
        } catch (Exception e) {
            logger.error("Can't retrive Jade config file successfully.", e);
        }
    }

    private void initJadeConfig() {
        String endpoints = config.getEndpoints();
        log.info("DbwolfMemZkXmlDocConfigurator connect to zk-sever " + endpoints);
        zkClient = new ZKClient(endpoints);
        retriveJadeConfig();
        watchJadeConfig();
    }

    /**
     * 监听Jade配置.
     * 
     * @param db
     */
    private void watchJadeConfig() {
        final String path = jadeConfigPath;
        log.debug("watch for jade config " + path);
        zkClient.registerDataListener(new DataListener() {

            public String getPath() {
                return path;
            }

            public void onDataChange() {
                try {
                    retriveJadeConfig();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
    }

}
