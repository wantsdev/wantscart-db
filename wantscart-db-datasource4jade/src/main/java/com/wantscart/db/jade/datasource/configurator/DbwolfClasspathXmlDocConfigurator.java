package com.wantscart.db.jade.datasource.configurator;

import com.wantscart.db.zookeeper.ZKClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Created by liujun on 15/5/23.
 */
@Component
public class DbwolfClasspathXmlDocConfigurator extends DbwolfXmlDocConfigurator {

    private static final Log log = LogFactory.getLog(DbwolfMemZkXmlDocConfigurator.class);

    private static final String DEFAULT_PATH = "jade/jade-config.xml";

    /**
     * 与zookeeper的连接.
     */
    private static ZKClient zkClient;

    private String jadeConfigPath;

    public DbwolfClasspathXmlDocConfigurator() {
        this(DEFAULT_PATH);
    }


    public DbwolfClasspathXmlDocConfigurator(String jadeConfigPath) {
        this.jadeConfigPath = jadeConfigPath;
        initJadeConfig();
    }

    /**
     * @return
     */
    private void retriveJadeConfig() {
        try {

            ClassPathResource classPathResource = new ClassPathResource(jadeConfigPath);
            int size = classPathResource.getInputStream().available();
            byte[] jadeConfig = new byte[size];

            classPathResource.getInputStream().read(jadeConfig);
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
