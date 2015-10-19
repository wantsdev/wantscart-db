package com.wantscart.db.jade.datasource;

import com.wantscart.db.client.ConnectionManager;
import com.wantscart.db.client.DbAgent;
import com.wantscart.jade.annotation.UseMaster;
import com.wantscart.jade.core.SQLThreadLocal;
import com.wantscart.jade.datasource.XnDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
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
        Connection conn;
        boolean write = false;
        String pattern = EMPTY_PATTERN;
        if (local == null) {
            write = true;
        } else {
            if (local.isWriteType()) {
                write = true;
            } else if (local.getModifier().getMethod().isAnnotationPresent(UseMaster.class)) {
                write = true;
            }
            pattern = (String) local.getParameters().get(DB_PATTERN);
            if (pattern == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("not found DB_PATTERN, using default patter '' for SQL '"
                            + local.getSql() + "'");
                }
                pattern = EMPTY_PATTERN;
            }
        }
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
