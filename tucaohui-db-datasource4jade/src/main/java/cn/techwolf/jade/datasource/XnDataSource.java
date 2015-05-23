package cn.techwolf.jade.datasource;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @author 廖涵 [in355hz@gmail.com]
 * 
 */
public abstract class XnDataSource implements DataSource {

    protected final Log logger = LogFactory.getLog(XnDataSource.class);

    public static final String DB_PATTERN = XnDataSource.class.getName() + "#DB_PATTERN";

    public static final String EMPTY_PATTERN = "";

    private String bizName;

    private XnDataSourceFactory dataSourceFactory;

    public XnDataSource() {
    }

    public XnDataSource(XnDataSourceFactory dataSourceFactory, String catalog) {
        this.dataSourceFactory = dataSourceFactory;
        setBizName(catalog);
    }

    public void setBizName(String bizName) {
        this.bizName = bizName;
    }

    public String getBizName() {
        return bizName;
    }

    public abstract Connection getConnection() throws SQLException;

    public Connection getConnection(String username, String password) throws SQLException {
        return getConnection();
    }

    //---------------------------------------

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {

    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {

    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    @Override
    public String toString() {
        return String.format("dataSource[bizName=%s]", bizName);
    }

    public void close() {
        if (dataSourceFactory != null) {
            dataSourceFactory.remove(this);
        }
    }

}
