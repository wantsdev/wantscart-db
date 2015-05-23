package cn.techwolf.dbwolf.client.config;

import java.util.Properties;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class EnvDbWolfConfig extends DbWolfConfig {

    private static final Log log = LogFactory.getLog(EnvDbWolfConfig.class);

    private static final String PROD_ENDPOINTS = "zk1:2181,zk2:2181,zk3:2181";

    private static final String DEV_ENDPOINTS = "192.168.1.11:2181,192.168.1.11:2182,192.168.1.12:2181";

    private static final String DEFAULT_FACTORY = "cn.techwolf.dbwolf.client.ds.DefaultJdbcDataSourceFactory";

    public static final String DEFAULT_ZKROOT = "/dbwolf";

    private static final String ENV_DS_ENDPOINTS_KEY = "zk_registry";

    private static final String ENV_DS_ZKROOT_KEY = "dbwolf_zkroot";

    private static final String ENV_DS_DSFACTORY_KEY = "dbwolf_dsfactory";
    
    /** zk_registry的设置 **/
    public static final String RESOURCE_NAME = "/registry.properties";

    public EnvDbWolfConfig() {
        AbstractConfiguration.setDefaultListDelimiter(';');
        this.conf = new BaseConfiguration();
        this.conf.addProperty("endpoints", getEnvEndpoints());
        this.conf.addProperty("dsfactory", getEnvDsfactory());
        this.conf.addProperty("dbwolf_zkroot", getEnvZKRoot());
    }

    /**
     * 获取Zookeeper 连接串，优先从JVM环境变量获取，系统环境变量次之，配置文件中的registry.properties，最后是默认值
     * 
     * @return Zookeeper 连接串
     */
    private String getEnvEndpoints() {
        String endpoints = null;
        endpoints = System.getProperty(ENV_DS_ENDPOINTS_KEY);
        if (endpoints == null) endpoints = System.getenv(ENV_DS_ENDPOINTS_KEY);
        try {
            Properties p = new Properties();
            p.load(EnvDbWolfConfig.class.getResourceAsStream(RESOURCE_NAME));
            if (p != null) {
                endpoints = p.getProperty(ENV_DS_ENDPOINTS_KEY);
            }
        } catch (Exception e) {
            log.warn("init getEnvEndpoints " + RESOURCE_NAME + ", " + e.getMessage());
        }
        
        if (endpoints == null) {
            endpoints = isDevEnv() ? DEV_ENDPOINTS : PROD_ENDPOINTS;
        }
        return endpoints;
    }

    /**
     * 获取具体的JdbcDataSourceFactory实现类，优先从JVM环境变量获取，系统环境变量次之，最后是默认值
     * 
     * @return
     */
    private String getEnvZKRoot() {
        String zkRoot = null;
        zkRoot = System.getProperty(ENV_DS_ZKROOT_KEY);
        if (zkRoot == null) zkRoot = System.getenv(ENV_DS_ZKROOT_KEY);
        if (zkRoot == null) {
            zkRoot = DEFAULT_ZKROOT;
        }
        return zkRoot;
    }

    /**
     * 获取具体的JdbcDataSourceFactory实现类，优先从JVM环境变量获取，系统环境变量次之，最后是默认值
     * 
     * @return
     */
    private String getEnvDsfactory() {
        String dsFactory = null;
        dsFactory = System.getProperty(ENV_DS_DSFACTORY_KEY);
        if (dsFactory == null) dsFactory = System.getenv(ENV_DS_DSFACTORY_KEY);
        if (dsFactory == null) {
            dsFactory = DEFAULT_FACTORY;
        }
        return dsFactory;
    }

    /**
     * 除非明确指明为Dev，否则是Production
     * 
     * @return
     */
    private boolean isDevEnv() {
        // Default:Production Env
        boolean isDev = false;

        String mode = System.getProperty("DBWOLF_MODE"); //jvm环境变量
        if (mode == null) mode = System.getenv("DBWOLF_MODE"); //系统环境变量
        if (mode == null) {
            /**
             * 自动根据网段选择
             */
            // if (isProdIp()) {
            isDev = false;
            // }
        } else if (mode.equalsIgnoreCase("dev")) {
            isDev = true;
        } else if (mode.equalsIgnoreCase("prod")) {
            isDev = false;
        } else {
            isDev = false;
        }

        log.info("DBWOLF_MODE dev is " + isDev);
        return isDev;
    }
    //    感觉通过IP段不太准确，现在的测试环境IP段也很多
    //    private boolean isProdIp() {
    //        String localIp = IPAddress.getLocalAddress();
    //        return localIp.startsWith("10.3.") || localIp.startsWith("10.6.")
    //                || localIp.startsWith("10.31.") || localIp.startsWith("10.10.")
    //                || localIp.startsWith("10.9.") || localIp.startsWith("10.4.");
    //    }

}
