package com.tucaohui.dbwolf.admin.config;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;

/**
 * 管理台配置.
 * 
 */
public final class AdminConfig {

    /**
     * 配置文件名称.
     */
    public static final String CONF_FILE = "dbwolf.properties";

    public static final String DEFAULT_ZKROOT = "/dbwolf";
    
    private final Configuration conf;

    private AdminConfig() {
        try {
            conf = new PropertiesConfiguration(CONF_FILE);
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public String getEndpoints() {
        return conf.getString("endpoints");
    }

    public String getZKRoot() {
        String zkroot = conf.getString("dbwolf_zkroot");
        if(StringUtils.isNotEmpty(zkroot)){
            return zkroot;
        }else{
            return DEFAULT_ZKROOT;
        }
    }
    
    public static AdminConfig getConfig() {
        return InstanceHolder.instance;
    }

    private static class InstanceHolder {

        public static final AdminConfig instance = new AdminConfig();
    }
}
