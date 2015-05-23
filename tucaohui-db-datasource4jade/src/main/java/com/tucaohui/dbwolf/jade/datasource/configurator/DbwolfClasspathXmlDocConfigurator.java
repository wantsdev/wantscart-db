package com.tucaohui.dbwolf.jade.datasource.configurator;

import cn.techwolf.dbwolf.zookeeper.ZKClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by liujun on 15/5/23.
 */
public class DbwolfClasspathXmlDocConfigurator extends DbwolfXmlDocConfigurator {



    private static final Log log = LogFactory.getLog(DbwolfMemZkXmlDocConfigurator.class);

    /**
     * 与zookeeper的连接.
     */
    private static ZKClient zkClient;

    private String jadeConfigPath;

    public DbwolfClasspathXmlDocConfigurator() {
        this(null);
    }

    public DbwolfClasspathXmlDocConfigurator(String jadeConfigPath) {
        this.jadeConfigPath = jadeConfigPath;
        initJadeConfig();
    }

    /**
     *
     * @return
     */
    private void retriveJadeConfig() {
        try {

            byte[] jadeConfig = null;
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
        retriveJadeConfig();
    }


}
