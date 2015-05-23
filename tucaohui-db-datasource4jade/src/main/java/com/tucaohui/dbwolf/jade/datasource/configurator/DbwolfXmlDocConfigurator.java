package com.tucaohui.dbwolf.jade.datasource.configurator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import com.tucaohui.jade.datasource.RoutingConfigurator;
import com.tucaohui.jade.datasource.RoutingDescriptor;
import com.tucaohui.jade.datasource.RoutingDescriptorLoader;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * 从: XML 文件中读取散表属性的配置器实现。
 * 
 * @author han.liao
 */
public class DbwolfXmlDocConfigurator implements RoutingConfigurator {

    // 连接配置服务器的超时
    public static final int CONNECT_TIMEOUT = 10000;

    // 读取配置服务器的超时
    public static final int READ_TIMEOUT = 10000;

    // 输出日志
    protected static final Log logger = LogFactory.getLog(DbwolfXmlDocConfigurator.class);

    // XmlDocXceConfigurator 实体处理器
    protected static final EntityResolver entityResolver = new EntityResolver() {

        @Override
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException,
                IOException {

            if (systemId.endsWith(JADE_CONFIG_DTD)) {

                ClassLoader classLoader = DbwolfXmlDocConfigurator.class.getClassLoader();

                URL url = classLoader.getResource(JADE_CONFIG_DTD);

                InputStream in;

                if (url != null) {

                    if (logger.isInfoEnabled()) {
                        logger.info( // NL
                        "Loading [" + JADE_CONFIG_DTD + "] from [" + url + ']');
                    }

                    in = url.openStream();

                } else {

                    if (logger.isWarnEnabled()) {
                        logger.warn( // NL
                        "Can't load [" + JADE_CONFIG_DTD + "] from [" + classLoader + ']');
                    }

                    in = new ByteArrayInputStream(new byte[0]);

                }

                return new InputSource(in);
            }

            return null;
        }
    };

    // XmlDocXceConfigurator 错误处理器
    protected static final ErrorHandler errorHandler = new ErrorHandler() {

        @Override
        public void warning(final SAXParseException exception) {

            if (logger.isWarnEnabled()) {
                logger.warn("Recoverable parsing error ", exception);
            }
        }

        @Override
        public void error(final SAXParseException exception) {

            if (logger.isWarnEnabled()) {
                logger.warn("Recoverable parsing error ", exception);
            }
        }

        @Override
        public void fatalError(final SAXParseException exception) {

            if (logger.isWarnEnabled()) {
                logger.warn("Fatal parsing error ", exception);
            }
        }
    };

    // 解析的配置项
    protected ConcurrentHashMap<String, RoutingDescriptor> map = new ConcurrentHashMap<String, RoutingDescriptor>();

    // 加锁保护配置信息
    protected ReadWriteLock rwLock = new ReentrantReadWriteLock();

    // 本地内存缓存的Jade_config内容,该文件不会特别大，就主站当前的也就100+K。
    protected byte[] jadeConfigContent = null;

    // 是否成功加载
    protected boolean inited;

    // 配置文件的  DTD
    private static final String JADE_CONFIG_DTD = "jade-config.DTD";

    public DbwolfXmlDocConfigurator() {

    }

    /**
     * 配置: XmlDocXceConfigurator 对象。
     */
    public DbwolfXmlDocConfigurator(byte[] xmlContent) {
        setJadeConfigContent(xmlContent);
    }

    /**
     * @return the jadeConfigContent
     */
    public byte[] getJadeConfigContent() {
        return jadeConfigContent;
    }

    /**
     * @param jadeConfigContent the jadeConfigContent to set
     */
    public void setJadeConfigContent(byte[] jadeConfigContent) {
        if (logger.isDebugEnabled()) {
            logger.debug("Jade config content:" + new String(jadeConfigContent));
        }
        // 加载配置文件。
        if (!loadXmlContent(jadeConfigContent)) {
            throw new IllegalArgumentException("Can't load jade xml content.");
        }

        this.jadeConfigContent = jadeConfigContent;
        inited = true;
    }

    @Override
    public RoutingDescriptor getCatalogDescriptor(String catalog) {

        if (!inited) {
            throw new IllegalStateException("XceConfigurator is not initialized.");
        }

        // 加锁保护配置信息的完整性
        Lock lock = rwLock.readLock();

        try {
            lock.lock();

            return map.get(catalog);

        } finally {

            lock.unlock();
        }
    }

    @Override
    public RoutingDescriptor getDescriptor(String catalog, String name) {

        if (!inited) {
            throw new IllegalStateException("XceConfigurator is not initialized.");
        }

        String keyword = catalog + '.' + name;

        // 加锁保护配置信息的完整性
        Lock lock = rwLock.readLock();

        try {
            lock.lock();

            RoutingDescriptor descriptor = map.get(keyword);

            if (descriptor == null) {

                descriptor = map.get(catalog); // 获取全局设置
            }

            return descriptor;

        } finally {

            lock.unlock();
        }
    }

    /**
     * 从指定的文件读取配置。
     * 
     * @param file - 指定的文件
     * 
     * @return 加载成功返回 <code>true</code>, 否则返回 <code>false</code>.
     */
    protected boolean loadXmlContent(byte[] xmlContent) {

        if (ArrayUtils.isEmpty(xmlContent)) {
            return false; // 空内容
        }

        Lock lock = null;

        try {
            // 打开  SAX DocumentBuilder
            DocumentBuilder db = createDocumentBuilder();

            InputStream xmlIs = new ByteArrayInputStream(xmlContent);
            // 打开文件流
            Document doc = db.parse(xmlIs);

            // 加锁保护配置信息的完整性
            lock = rwLock.writeLock();

            lock.lock();

            // 清除配置信息
            map.clear();

            // 加载配置信息
            RoutingDescriptorLoader.loadXMLDoc(map, doc);

            return true;

        } catch (SAXException e) {

            // 输出日志
            if (logger.isWarnEnabled()) {
                logger.warn("Can't parse xml byte array content.", e);
            }

        } catch (IOException e) {

            // 输出日志
            if (logger.isWarnEnabled()) {
                logger.warn("Can't parse xml byte array content.", e);
            }

        } finally {

            if (lock != null) {
                lock.unlock();
            }
        }

        return false;
    }

    /**
     * 返回新建的: SAX DocumentBuilder 对象。
     * 
     * @return SAX DocumentBuilder 对象
     */
    private static DocumentBuilder createDocumentBuilder() {

        DocumentBuilderFactory dbf = null;

        try {

            dbf = DocumentBuilderFactory.newInstance();

            if (logger.isDebugEnabled()) {
                logger.debug("Using DocumentBuilderFactory: " + dbf.getClass().getName());
            }

        } catch (FactoryConfigurationError e) {

            if (logger.isWarnEnabled()) {
                logger.warn("Could not instantiate a DocumentBuilderFactory.", e.getException());
            }

            throw e;
        }

        try {
            dbf.setValidating(false);

            DocumentBuilder db = dbf.newDocumentBuilder();

            db.setErrorHandler(errorHandler);
            db.setEntityResolver(entityResolver);

            return db;

        } catch (ParserConfigurationException e) {

            if (logger.isWarnEnabled()) {
                logger.warn("Could not instantiate a DocumentBuilder.", e);
            }

            throw new IllegalArgumentException("Could not instantiate a DocumentBuilder.", e);
        }
    }

}
