package com.tucaohui.dbwolf.client.ds;

import com.tucaohui.dbwolf.client.DbAgent;
import cn.techwolf.dbwolf.xml.DbInstanceConfig;
import cn.techwolf.dbwolf.xml.RouteConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.sql.DataSource;

/**
 * 可路由数据源.
 * 
 */
public class RouterDsPool implements StormDataSourcePool {

    public static Log log = LogFactory.getLog(RouterDsPool.class);

    private final DbAgent agent;

    private final DbInstanceConfig config;

    public RouterDsPool(final DbAgent agent, final DbInstanceConfig config) {
        this.agent = agent;
        this.config = config;
    }

    public DataSource getReadableDs(final String pattern) {
        return findDataSource(pattern).getReadableDs(pattern);
    }

    public DataSource getWriteableDs(final String pattern) {
        return findDataSource(pattern).getWriteableDs(pattern);
    }

    /**
     * 按表达式找到匹配数据源.
     * 
     * @param pattern 匹配模式
     * @return
     */
    protected StormDataSourcePool findDataSource(final String pattern) {
        for (RouteConfig route : config.getRoutes()) {
            log.debug("Comparing " + pattern + " aginst " + route.getExpression());
            if (pattern.matches(route.getExpression())) {
                return agent.getDsPool(route.getInstance());
            }
        }
        throw new NoRouteMatchExecption(pattern, config.getName());
    }

    public long getTimeStamp() {
        return config.getTimestamp();
    }

    public void close() {
        log.debug("close route " + config.getName());
        //什么也不做
    }

    @Override
    public String toString() {
        return "RouterInstance: " + config.getName();
    }
}
