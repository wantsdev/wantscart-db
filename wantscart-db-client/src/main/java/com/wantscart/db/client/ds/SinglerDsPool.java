package com.wantscart.db.client.ds;

import com.wantscart.db.xml.DbInstanceConfig;
import com.wantscart.db.xml.DbServerConfig;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;


/**
 * 单实例数据源配置.
 * 
 */
public class SinglerDsPool implements StormDataSourcePool {

    private final DbInstanceConfig config;

    private DataSource writer;

    private final List<DataSource> readers = new ArrayList<DataSource>();

    public SinglerDsPool(final DbInstanceConfig config) {
        this.config = config;
    }

    private synchronized void buildWriteableDs() {
        if (null != writer) {
            return;
        }
        writer = buildJdbcDs(config.getWserver());
    }

    public DataSource getWriteableDs(final String pattern) {
        buildWriteableDs();
        return writer;
    }

    public DataSource getReadableDs(final String pattern) {
        buildReadableDs();
        checkReadableDs();
        return readers.get((int) (Math.random() * readers.size()));
    }

    void checkReadableDs() {
        if (readers.size() <= 0) throw new NoReadableDsDefineException(config.getName());
    }

    private synchronized void buildReadableDs() {
        if (readers.size() != 0) {
            return;
        }
        for (int buildReader = 0; buildReader < config.getRservers().size(); ++buildReader) {
            readers.add(buildJdbcDs(config.getRservers().get(buildReader)));
        }
    }

    public long getTimeStamp() {
        return config.getTimestamp();
    }

    public void close() {
        closeDataSource(writer);
        for (DataSource reader : readers) {
            closeDataSource(reader);
        }
    }

    private synchronized DataSource buildJdbcDs(final DbServerConfig config) {
        return getFactory().createDataSource(config);
    }

    private void closeDataSource(final DataSource ds) {
        getFactory().closeDataSource(ds);
    }

    private JdbcDataSourceFactory getFactory() {
        return JdbcDsFactoryManager.getFactory();
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("SinglerInstance: ").append(config.getName()).append(" ");
        if (writer != null) {
            buf.append("W[").append(getFactory().describeDataSource(writer)).append("] ");
        }
        if (readers != null) {
            for (int x = 0; x < readers.size(); ++x) {
                if (readers.get(x) != null) {
                    buf.append("R" + x + "[")
                            .append(getFactory().describeDataSource(readers.get(x))).append("] ");
                }
            }
        }
        return buf.toString();
    }

}
