package com.wantscart.db.client.config;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class DbWolfConfig {

    private static final Log log = LogFactory.getLog(DbWolfConfig.class);

    private static final DbWolfConfig defaultConfig = new EnvDbWolfConfig();

    protected Configuration conf;

    public String getEndpoints() {
        return conf.getString("endpoints");
    }

    public String getDsFactoryClz() {
        return conf.getString("dsfactory");
    }

    public String getZkRoot() {
        return conf.getString("dbwolf_zkroot");
    }

    public static DbWolfConfig getConfig() {
        return InstanceHolder.instance;
    }

    private static class InstanceHolder {

        public static final DbWolfConfig instance = createConfig();

        private static DbWolfConfig createConfig() {
            DbWolfConfig config = null;
            try {
                config = new FileDbWolfConfig(defaultConfig);
            } catch (Throwable e) {
                config = defaultConfig;
                log.info("fail to read config file, use default set ");
            }
            return config;
        }
    }

}
