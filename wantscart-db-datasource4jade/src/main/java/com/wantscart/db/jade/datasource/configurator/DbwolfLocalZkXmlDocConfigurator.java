package com.wantscart.db.jade.datasource.configurator;

import com.wantscart.db.client.config.DbWolfConfig;
import com.wantscart.db.zookeeper.DataDeserializer;
import com.wantscart.db.zookeeper.DataListener;
import com.wantscart.db.zookeeper.ZKClient;
import com.wantscart.db.zookeeper.exception.ZKDataDeserializeException;
import com.wantscart.jade.datasource.configurator.XmlDocConfigurator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;

/**
 * 
 * @author <a href="mailto:tao.zhang@renren-inc.com">Kylen Zhang</a>
 *         Initial created at 2012-10-24 下午03:34:30
 */
public class DbwolfLocalZkXmlDocConfigurator extends XmlDocConfigurator {

    private static final Log log = LogFactory.getLog(DbwolfLocalZkXmlDocConfigurator.class);

    // 本地配置文件 - Linux
    public static final String LOCAL_PATH_LINUX = "/etc/dbwolf-jade-config/";

    // 本地配置文件 - Windows
    public static final String LOCAL_PATH_WINDOWS = "C:\\dbwolf-jade-config\\";

    // 标记文件名称
    public static final String MARKUP_FILE = "use-local-config";

    // 配置文件名称
    public static final String CONFIG_FILE = "jade-config.xml";

    /**
     * zookeeper根路径.
     */
    public static final String JADE_CONFIG_PATH = DbWolfConfig.getConfig().getZkRoot()
            + "/jade-config";

    /**
     * 与zookeeper的连接.
     */
    private static ZKClient zkClient;

    //    static {
    //        initJadeConfig();
    //    }

    public DbwolfLocalZkXmlDocConfigurator() {
        initJadeConfig();
        this.setFile(getLocalLoadingFile());
    }

    /**
     * 
     * @return
     */
    private void retriveJadeConfig() {
        try {
            byte[] jadeConfig = zkClient.syncReadData(JADE_CONFIG_PATH,
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
                saveToFile(jadeConfig, getLocalLoadingFile());
            }
        } catch (Exception e) {
            logger.error("Can't retrive Jade config file successfully.", e);
        }
    }

    private void initJadeConfig() {
        String endpoints = DbWolfConfig.getConfig().getEndpoints();
        log.info("DbwolfLocalZkXmlDocConfigurator connect to zk-sever " + endpoints);
        zkClient = new ZKClient(endpoints);
        retriveJadeConfig();
        watchJadeConfig();
    }

    /**
     * 监听Jade配置.
     *
     * @param
     */
    private void watchJadeConfig() {
        final String path = JADE_CONFIG_PATH;
        log.debug("watch for jade config " + path);
        zkClient.registerDataListener(new DataListener() {

            public String getPath() {
                return path;
            }

            public void onDataChange() {
                try {
                    retriveJadeConfig();
                    setFile(getLocalLoadingFile());
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
    }

    /**
     * 读取网址内容存入文件。
     * 
     * @param jadeConfigContent - JadeConfig内容
     * @param file - 存入的文件
     */
    private void saveToFile(byte[] jadeConfigContent, File file) {
        // 创建文件的目录
        File dir = file.getParentFile();
        if (dir != null) {
            if (!dir.mkdirs()) {

                // 输出日志
                if (logger.isWarnEnabled()) {
                    logger.warn("Can't make dir: " + dir.getPath());
                }
            }
        }
        InputStream fin = null;
        FileOutputStream fout = null;
        try {
            fin = new ByteArrayInputStream(jadeConfigContent);
            // 将内容写入文件
            fout = new FileOutputStream(file);
            byte[] buffer = new byte[4096];
            int read = fin.read(buffer);
            while (read >= 0) {
                fout.write(buffer, 0, read);
                read = fin.read(buffer);
            }
            fout.flush();
        } catch (IOException e) {
            // 输出日志
            if (logger.isWarnEnabled()) {
                logger.warn("Can't save InputStream to: " + file.getPath());
            }
        } finally {
            try {
                if (fout != null) {
                    fout.close();
                }
            } catch (IOException e) {
                // 输出日志
                if (logger.isWarnEnabled()) {
                    logger.warn("Can't close FileOutputStream");
                }
            }
            try {
                if (fin != null) {
                    fin.close();
                }
            } catch (IOException e) {
                // 输出日志
                if (logger.isWarnEnabled()) {
                    logger.warn("Can't close InputStream");
                }
            }
        }
    }

    /**
     * 返回从本地加载的文件路径。
     * 
     * @return 从本地加载的文件路径
     */
    private File getLocalLoadingFile() {

        String filePath;

        if (File.separatorChar == '\\') {

            filePath = LOCAL_PATH_WINDOWS + CONFIG_FILE;

        } else {

            filePath = LOCAL_PATH_LINUX + CONFIG_FILE;
        }

        return new File(filePath);
    }

}
