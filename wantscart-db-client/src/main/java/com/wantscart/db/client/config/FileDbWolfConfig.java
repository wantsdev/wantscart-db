package com.wantscart.db.client.config;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * dbwolf系统全局配置.
 * 
 */
public class FileDbWolfConfig extends DbWolfConfig {

    /**
     * 配置文件名称.
     */
    public static final String CONF_FILE = "dbwolf.properties";

    private Configuration conf;

    private DbWolfConfig defaultConfig;

    protected FileDbWolfConfig(DbWolfConfig defaultConfig) {
        this.defaultConfig = defaultConfig;
        try {
            AbstractConfiguration.setDefaultListDelimiter(';');
            conf = new PropertiesConfiguration(CONF_FILE);
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public String getEndpoints() {
        return conf.getString("endpoints", defaultConfig.getEndpoints());
    }

    public String getDsFactoryClz() {
        return conf.getString("dsfactory", defaultConfig.getDsFactoryClz());
    }

    public String getZkRoot() {
        return conf.getString("dbwolf_zkroot", defaultConfig.getZkRoot());
    }
}
