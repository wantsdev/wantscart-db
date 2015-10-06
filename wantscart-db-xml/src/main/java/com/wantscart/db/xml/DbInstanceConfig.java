package com.wantscart.db.xml;

import java.util.ArrayList;
import java.util.List;

import com.wantscart.db.xml.DbXmlParser.DbInstanceType;

/**
 * 虚拟数据库配置. immutable
 * 
 */
public class DbInstanceConfig {

    private String name;

    private long timestamp;

    private DbInstanceType type;

    private DbServerConfig wserver;

    private final List<DbServerConfig> rservers = new ArrayList<DbServerConfig>();

    private final List<RouteConfig> routes = new ArrayList<RouteConfig>();

    private String xml;

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        sb.append("name:");
        sb.append(name);
        sb.append(",");
        sb.append("timestamp:");
        sb.append(timestamp);
        sb.append(",");
        sb.append("type:");
        sb.append(type);
        sb.append(",");
        sb.append("wserver:");
        sb.append(String.valueOf(wserver));
        sb.append(",");
        sb.append("rservers:");
        sb.append(String.valueOf(rservers));
        sb.append(",");
        sb.append("routes:");
        sb.append(routes.toString());
        sb.append("}");
        return sb.toString();
    }

    public String getName() {
        return name;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public DbInstanceType getType() {
        return type;
    }

    public DbServerConfig getWserver() {
        return wserver;
    }

    public List<DbServerConfig> getRservers() {
        ArrayList<DbServerConfig> copy = new ArrayList<DbServerConfig>(rservers.size());
        copy.addAll(rservers);
        return copy;
    }

    public List<RouteConfig> getRoutes() {
        ArrayList<RouteConfig> copy = new ArrayList<RouteConfig>(routes.size());
        copy.addAll(routes);
        return copy;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setType(final DbInstanceType type) {
        this.type = type;
    }

    public void setWserver(final DbServerConfig wserver) {
        this.wserver = wserver;
    }

    public void setRservers(final List<DbServerConfig> configs) {
        rservers.clear();
        rservers.addAll(configs);
    }

    public void setRoutes(final List<RouteConfig> configs) {
        routes.clear();
        routes.addAll(configs);
    }

    public String getXml() {
        return xml;
    }

    public void setXml(final String xml) {
        this.xml = xml;
    }

}
