package com.wantscart.db.xml;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * 解析dbwolf的全局数据库配置文件.
 * 
 * 每个InstancesXmlParser实例都是一次性的.
 * 
 */
public class InstancesXmlParser {

    private String source;

    private final List<DbInstanceConfig> configs = new ArrayList<DbInstanceConfig>();

    private boolean parsed = false;

    /**
     * @param source 配置文件路径
     * @throws StromXmlConfigException
     */
    public InstancesXmlParser(final String source) throws StromXmlConfigException {
        this.source = source;
        doParse();
    }

    /**
     * 配置文件url
     * 
     * @param url
     * @throws StromXmlConfigException
     */
    public InstancesXmlParser(final URL url) throws StromXmlConfigException {
        this.source = url.getFile();
        doParse();
    }

    /**
     * 开始进行解析.
     * 
     * @return
     * @throws StromXmlConfigException
     */
    @SuppressWarnings("unchecked")
    private synchronized InstancesXmlParser doParse() throws StromXmlConfigException {
        Document document = createDocument();
        Element root = getRoot(document);
        for (Iterator<Element> it = root.elementIterator("instance"); it.hasNext();) {
            Element instanceConf = it.next();
            String xml = instanceConf.asXML();
            DbXmlParser dbConfig;
            try {
                dbConfig = new DbXmlParser(xml);
                configs.add(dbConfig.getDbInstanceConfig());
            } catch (InstanceXmlConfigException e) {
                StromXmlConfigException ex = new StromXmlConfigException(e);
                ex.setXml(xml);
                throw ex;
            }

        }
        parsed = true;
        return this;
    }

    /**
     * 获取全部解析结果.
     * 
     * @return
     */
    public synchronized List<DbInstanceConfig> getDbInstances() {
        checkParsed();
        return configs;
    }

    /**
     * 判断是否已经完成配置解析.
     */
    private void checkParsed() {
        if (!parsed) throw new IllegalStateException(
                "can not perform action before call doParse() method");
    }

    /**
     * 将指定的source解析为Dom.
     * 
     * @return
     */
    private Document createDocument() throws StromXmlConfigException {
        SAXReader reader = new SAXReader();
        Document document = null;
        try {
            document = reader.read(this.source);
        } catch (DocumentException e) {
            throw new StromXmlConfigException("xml config error", e);
        } finally {}
        return document;
    }

    /**
     * 提取dom跟节点.
     * 
     * @param document
     * @return
     */
    private Element getRoot(final Document document) {
        return document.getRootElement();
    }

}
