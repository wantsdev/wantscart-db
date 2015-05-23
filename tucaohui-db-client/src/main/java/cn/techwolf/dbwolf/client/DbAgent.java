package cn.techwolf.dbwolf.client;

import cn.techwolf.dbwolf.client.ds.StormDataSourcePool;

/**
 * Created by liujun on 15/5/23.
 */
public interface DbAgent {

    public StormDataSourcePool getDsPool(final String db);

    public ConnectionManager getConnectionManager() throws DbAgentInitException;

}
