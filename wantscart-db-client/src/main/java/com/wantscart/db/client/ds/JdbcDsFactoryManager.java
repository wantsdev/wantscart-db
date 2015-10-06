package com.wantscart.db.client.ds;

import com.wantscart.db.client.config.DbWolfConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * jdbc数据源管理器.
 * 
 */
public class JdbcDsFactoryManager {

    public static final Log log = LogFactory.getLog(JdbcDsFactoryManager.class);

    /**
     * 默认工厂实现.
     */
    public static final String DEFAULT_FACTORY_CLASS = "DefaultJdbcDataSourceFactory";

    /**
     * 指定工厂类属性.
     */
    public static final String FACTORY_KEY = "dsfactory";

    /**
     * 获取一个工厂实例.
     * 
     * @return 工厂实例
     */
    public static JdbcDataSourceFactory getFactory() {
        return FactoryHolder.factory;
    }

    private static class FactoryHolder {

        public static final JdbcDataSourceFactory factory = createFactory();

        /**
         * 加载工厂类并创建实例.
         * 
         * @return 工厂实例
         */
        private static JdbcDataSourceFactory createFactory() {
            String factoryClazz = getFactoryClazz();
            Class<?> clz = null;
            try {
                ClassLoader loader = Thread.currentThread().getContextClassLoader();//使用contextloader加载
                clz = loader.loadClass(factoryClazz);
                return (JdbcDataSourceFactory) clz.newInstance();
            } catch (Exception e) {
                log.error(" jdbcdsfactory class [" + factoryClazz
                        + "] not found ,use default factory");
                return new DefaultJdbcDataSourceFactory();
            }
        }
    }

    /**
     * 取得工厂类信息.
     * 
     * @return 工厂实现类名
     */
    private static String getFactoryClazz() {
        String factoryClass = DEFAULT_FACTORY_CLASS;
        DbWolfConfig config = DbWolfConfig.getConfig();
        String clz = config.getDsFactoryClz();
        if (clz != null) factoryClass = clz;
        log.info(" use [ " + factoryClass + "] as default datasource factory");
        return factoryClass;
    }
}
