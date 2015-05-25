package com.tucaohui.dbwolf.jade.datasource;

import com.tucaohui.dbwolf.client.ConnectionManager;
import com.tucaohui.dbwolf.client.DbAgent;
import com.tucaohui.jade.annotation.UseMaster;
import com.tucaohui.jade.core.SQLThreadLocal;
import com.tucaohui.jade.datasource.XnDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * 
 * @author <a href="mailto:tao.zhang@renren-inc.com">Kylen Zhang</a>
 *         Initial created at 2012-10-23 下午05:04:06
 */
public class DbwolfDataSource extends XnDataSource {

    protected final Log logger = LogFactory.getLog(DbwolfDataSource.class);

    private DbwolfDataSourceFactory dsFactory;

    private ConnectionManager connectionManager;
    
    public DbwolfDataSource() {

    }

    public DbwolfDataSource(DbwolfDataSourceFactory dataSourceFactory, String catalog, DbAgent dbAgent) {
        connectionManager = dbAgent.getConnectionManager();
        dsFactory = dataSourceFactory;
        setBizName(catalog);
    }

    public Connection getConnection() throws SQLException {
        SQLThreadLocal local = SQLThreadLocal.get();
        Assert.notNull(local, "this is jade's bug; class SQLThreadLocalWrapper "
                + "should override all the DataAccess interface methods.");
        boolean write = false;
        if (local.isWriteType()) {
            write = true;
        } else if (local.getModifier().getMethod().isAnnotationPresent(UseMaster.class)) {
            write = true;
        }
        String pattern = (String) local.getParameters().get(DB_PATTERN);
        if (pattern == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("not found DB_PATTERN, using default patter '' for SQL '"
                        + local.getSql() + "'");
            }
            pattern = EMPTY_PATTERN;
        }
        Connection conn;
        if (write) {
            conn = connectionManager.getWriteConnection(getBizName(), pattern);
        } else {
            conn = connectionManager.getReadConnection(getBizName(), pattern);
        }
        if (conn == null) {
            throw new SQLException("could't get " + (write ? "Write" : "Read")
                    + " connection from bizName '" + getBizName() + "' for pattern '" + pattern
                    + "'");
        }
        return conn;
    }

    public Connection getConnection(String username, String password) throws SQLException {
        return this.getConnection();
    }

    public void close() {
        if (dsFactory != null) {
            dsFactory.remove(this);
        }
    }

    /* (non-Javadoc)
     * @see javax.sql.CommonDataSource#getParentLogger()
     */
    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new UnsupportedOperationException();
    }
    
    
}
