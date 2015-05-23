package cn.techwolf.dbwolf.client.ds;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.techwolf.dbwolf.xml.DbServerConfig;

/**
 * 默认的jdbc数据源工厂实现. 使用dbcp作为数据源.
 * 
 */
public final class DefaultJdbcDataSourceFactory implements JdbcDataSourceFactory {

    public static final Log log = LogFactory.getLog(DefaultJdbcDataSourceFactory.class);

    public static final String DB_TYPE_MYSQL = "mysql";

    public static final String DB_TYPE_POSTGRESQL = "postgresql";

    public static final String CLASSNAME_MYSQL = "com.mysql.jdbc.Driver";

    public static final String CLASSNAME_POSTGRESQL = "org.postgresql.Driver";

    public static final String EMPTY_PATTERN = "";

    public DataSource createDataSource(final DbServerConfig server) {
        if (server.getType().equalsIgnoreCase(DB_TYPE_MYSQL)) {
            return createMySQLDataSource(server);
        } else if (server.getType().equalsIgnoreCase(DB_TYPE_POSTGRESQL)) {
            return createPostGreDataSource(server);
        } else {
            log.error(" Invalid Database server type:" + server.getType());
            return null;
        }
    }

    public DataSource createPostGreDataSource(final DbServerConfig server) {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(CLASSNAME_POSTGRESQL);
        ds.setUrl("jdbc:postgresql://" + server.getHost()
                + (server.getPort() == 0 ? "" : ":" + String.valueOf(server.getPort())) + "/"
                + server.getDatabase());
        // + "?user=" + server.getUser() + "&password="
        // + server.getPassword()
        // + "&loginTimeout=100&autoReconnect=true&charSet="
        // + String.valueOf(server.getCharset()));
        ds.setUsername(server.getUser());
        ds.setPassword(server.getPassword());
        ds.setInitialSize(0);// This ensures no idle connection which only for
        // initialize.
        ds.setMaxActive(1000);
        ds.setMinIdle(0);
        ds.setMaxIdle(ds.getMaxActive() / 10 + 1);
        ds.setMaxWait(10);
        ds.setTestOnReturn(false);
        ds.setTestWhileIdle(true);
        ds.setMinEvictableIdleTimeMillis(10000);
        ds.setTimeBetweenEvictionRunsMillis(10000);
        ds.setNumTestsPerEvictionRun(2);
        ds.setValidationQuery("SELECT 5");

        return ds;
    }

    /**
     * 创建MySQL DataSource
     * 
     * @param server
     * @return
     */
    public DataSource createMySQLDataSource(final DbServerConfig server) {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(CLASSNAME_MYSQL);
        ds.setUrl("jdbc:mysql://" + server.getHost()
                + (server.getPort() == 0 ? "" : ":" + String.valueOf(server.getPort())) + "/"
                + server.getDatabase() + "?user=" + server.getUser() + "&password="
                + server.getPassword()
                + "&connectTimeout=3000&autoReconnect=true&zeroDateTimeBehavior=convertToNull&characterEncoding="
                + String.valueOf(server.getCharset()));
        ds.setUsername(server.getUser());
        ds.setPassword(server.getPassword());
        ds.setInitialSize(0);// This ensures no idle connection which only for
        // initialize.
        ds.setMaxActive(1000);
        ds.setMinIdle(0);
        ds.setMaxIdle(ds.getMaxActive() / 10 + 1);
        ds.setMaxWait(10);
        ds.setTestOnReturn(false);
        ds.setTestWhileIdle(true);
        ds.setMinEvictableIdleTimeMillis(10000);
        ds.setTimeBetweenEvictionRunsMillis(10000);
        ds.setNumTestsPerEvictionRun(2);
        ds.setValidationQuery("SELECT 5");
        return ds;
    }

    /**
     * 关闭数据源.
     */
    public void closeDataSource(final DataSource ds) {
        if (ds instanceof BasicDataSource) {
            BasicDataSource new_name = (BasicDataSource) ds;
            try {
                new_name.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public String describeDataSource(DataSource ds) {
        StringBuffer buf = new StringBuffer();
        if (ds != null) {
            buf.append(((BasicDataSource) ds).getNumActive()).append(",")
                    .append(((BasicDataSource) ds).getNumIdle());
        }
        return buf.toString();
    }

}
