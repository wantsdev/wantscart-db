package com.wantscart.db.xml;

import java.io.Reader;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * 解析一个或多个instance配置.
 * 
 */
public class DbXmlParser {

    /**
     * 数据服务类型.
     * 
     * @author huaiyu.du@opi-corp.com 2012-2-8 下午5:40:56
     */
    public enum DbInstanceType {
        CDbInstanceSingler, CDbInstanceRouter
    }

    /**
     * 数据库服务器读写类型.
     * 
     * @author huaiyu.du@opi-corp.com 2012-2-8 下午5:41:11
     */
    public enum DbServerType {
        CDbRServer, CDbWServer, CDbWRServer
    }

    private String source;

    private List<DbInstanceConfig> configs = new ArrayList<DbInstanceConfig>();

    private boolean parsed = false;

    public DbXmlParser(final String source) throws InstanceXmlConfigException {
        this.source = source;
        doParse();
    }

    /**
     * 开始进行解析.
     * 
     * @return
     * @throws InstanceXmlConfigException
     */
    private synchronized DbXmlParser doParse() throws InstanceXmlConfigException {
        Document document = createDocument();
        DbInstanceConfig instance = parseDbInstance(getRoot(document));
        configs.add(instance);
        parsed = true;
        return this;
    }

    public synchronized DbInstanceConfig getDbInstanceConfig() {
        checkParsed();
        if (configs.size() > 0) return configs.get(0);
        else return null;
    }

    /**
     * 判断是否已经完成配置解析.
     */
    private void checkParsed() {
        if (!parsed) throw new IllegalStateException(
                "can not perform action before call doParse() method");
    }

    /**
     * 解析descriptor实例.
     * 
     * @param conf
     * @return
     * @throws InstanceXmlConfigException
     */
    @SuppressWarnings("unchecked")
    private DbInstanceConfig parseDbInstance(final Element conf) throws InstanceXmlConfigException {
        DbInstanceConfig instance = new DbInstanceConfig();
        instance.setXml(conf.asXML());
        for (Iterator<Attribute> ait = conf.attributeIterator(); ait.hasNext();) {
            Attribute attr = ait.next();
            if (attr.getName().equals("name")) {
                instance.setName(attr.getValue());
            }
            if (attr.getName().equals("type")) {
                instance.setType(XmlConfigUtil.parseInstanceType(attr.getValue()));
            }
            if (attr.getName().equals("timestamp")) {
                try {
                    instance.setTimestamp(XmlConfigUtil.parseTimeStamp(attr.getValue()));
                } catch (ParseException e) {
                    throw new InstanceXmlConfigException(this.source, e);
                }
            }
        }
        if (instance.getType() == DbInstanceType.CDbInstanceSingler) {
            parseDbServer(conf, instance);
        } else {
            parseRouteServer(conf, instance);
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    private DbInstanceConfig parseRouteServer(final Element conf, final DbInstanceConfig instance) {
        if (instance == null) throw new IllegalArgumentException("instance can not be null");
        for (Iterator<Element> it = conf.elementIterator("route"); it.hasNext();) {
            Element serverConf = it.next();
            RouteConfig routeServer = new RouteConfig();
            for (Iterator<Attribute> ait = serverConf.attributeIterator(); ait.hasNext();) {
                Attribute attr = ait.next();
                if (attr.getName().equals("instance")) {
                    routeServer.setInstance(attr.getValue());
                }
                if (attr.getName().equals("expression")) {
                    routeServer.setExpression(attr.getValue());
                }
                List<RouteConfig> routes = instance.getRoutes();
                routes.add(routeServer);
                instance.setRoutes(routes);
            }
        }
        return instance;
    }

    /**
     * 解析数据库实例配置.
     * 
     * @param conf
     * @param instance
     * @return
     */
    @SuppressWarnings("unchecked")
    private DbInstanceConfig parseDbServer(final Element conf, final DbInstanceConfig instance) {
        if (instance == null) throw new IllegalArgumentException("instance can not be null");
        for (Iterator<Element> it = conf.elementIterator("server"); it.hasNext();) {
            Element serverConf = it.next();
            DbServerConfig dbServer = new DbServerConfig();
            for (Iterator<Attribute> ait = serverConf.attributeIterator(); ait.hasNext();) {
                Attribute attr = ait.next();
                if (attr.getName().equals("type")) {
                    dbServer.setType(attr.getValue());
                }
                if (attr.getName().equals("database")) {
                    dbServer.setDatabase(attr.getValue());
                }
                if (attr.getName().equals("host")) {
                    dbServer.setHost(attr.getValue());
                }
                if (attr.getName().equals("port")) {
                    dbServer.setPort(XmlConfigUtil.parseInt(attr.getValue()));
                }
                if (attr.getName().equals("user")) {
                    dbServer.setUser(attr.getValue());
                }
                if (attr.getName().equals("password")) {
                    dbServer.setPassword(attr.getValue());
                }
                if (attr.getName().equals("charset")) {
                    dbServer.setCharset(attr.getValue());
                }
                if (attr.getName().equals("wrflag")) {
                    DbServerType serverType = XmlConfigUtil.parseServerType(attr.getValue());
                    if (serverType == DbServerType.CDbWServer) instance.setWserver(dbServer);
                    if (serverType == DbServerType.CDbRServer) {
                        List<DbServerConfig> rservers = instance.getRservers();
                        rservers.add(dbServer);
                        instance.setRservers(rservers);
                    }
                    if (serverType == DbServerType.CDbWRServer) {
                        instance.setWserver(dbServer);
                        List<DbServerConfig> rservers = instance.getRservers();
                        rservers.add(dbServer);
                        instance.setRservers(rservers);
                    }
                }
            }
        }
        return instance;
    }

    /**
     * 将指定的source解析为Dom.
     * 
     * @return
     * @throws InstanceXmlConfigException
     */
    private Document createDocument() throws InstanceXmlConfigException {
        SAXReader reader = new SAXReader();
        Document document = null;
        Reader sreader = new StringReader(this.source);
        try {
            document = reader.read(sreader);
        } catch (DocumentException e) {
            throw new InstanceXmlConfigException(this.source, e);
        }
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
