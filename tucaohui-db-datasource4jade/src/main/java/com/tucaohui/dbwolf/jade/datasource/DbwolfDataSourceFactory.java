package com.tucaohui.dbwolf.jade.datasource;

import cn.techwolf.dbwolf.client.DbAgent;
import cn.techwolf.jade.annotation.DAO;
import cn.techwolf.jade.datasource.DataSourceFactory;
import com.tucaohui.jade.datasource.XnDataSource;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * @author <a href="mailto:zhangtao@techwolf.cn">Kylen Zhang</a> Initial
 *         created at 2014年3月11日下午1:59:40
 * 
 */
public class DbwolfDataSourceFactory implements DataSourceFactory {

    private Map<String, DataSource> cached = new ConcurrentHashMap<String, DataSource>();

    private DbAgent dbAgent;

    @Override
    public DataSource getDataSource(Class<?> daoClass) {
        String catalog = daoClass.getAnnotation(DAO.class).catalog();
        DataSource dataSource = null;
        if (catalog != null && catalog.length() > 0) {
            dataSource = cached.get(catalog);
            if (dataSource == null) {
                dataSource = new DbwolfDataSource(this, catalog, dbAgent);
                cached.put(catalog, dataSource);
            }
        }
        return dataSource;
    }

    public void remove(XnDataSource xnDataSource) {
        cached.remove(xnDataSource.getBizName());
    }

    /**
     * @param dbAgent the dbAgent to set
     */
    public void setDbAgent(DbAgent dbAgent) {
        this.dbAgent = dbAgent;
    }

}
