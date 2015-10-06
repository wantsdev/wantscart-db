package com.wantscart.db.client;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 数据库连接管理器. <br>
 * 此类是client层与jdbc层的唯一接口，jdbc层只需要调用此类中的方法就可以获得java.sql.Connection.
 * 
 */
public final class ConnectionManager {

    public static final String EMPTY_PATTERN = "";

    private final DbAgent agent; //private型，且不提供getter方法

    /**
     * 创建一个连接管理器.
     * 
     * @param agent
     */
    ConnectionManager(DbAgent agent) {
        this.agent = agent;
    }

    /**
     * 获取不可路由数据服务的一个只读数据连接. <br>
     * <strong>注意：</strong>使用前请确认pattern对应的数据库确实是可读的
     * 
     * @param db 数据服务名称
     * @return jdbc连接
     * @throws SQLException
     */
    public Connection getReadConnection(String db) throws SQLException {
        Connection conn = agent.getDsPool(db).getReadableDs(EMPTY_PATTERN).getConnection();
        return conn;
    }

    /**
     * 从可路由数据服务中寻找指定pattern对应的具体数据库，然后获取一个只读数据库连接. <br>
     * <strong>注意：</strong>使用前请确认pattern对应的数据库确实是可读的
     * 
     * @param db 数据服务名称
     * @return jdbc连接
     * @throws SQLException
     */
    public Connection getReadConnection(String db, String pattern) throws SQLException {
        return agent.getDsPool(db).getReadableDs(pattern).getConnection();
    }

    /**
     * 获取不可路由数据服务的一个只写数据连接. <br>
     * <strong>注意：</strong>使用前请确认pattern对应的数据库确实是可写的
     * 
     * @param db 数据服务名称
     * @return jdbc连接
     * @throws SQLException
     */
    public Connection getWriteConnection(String db) throws SQLException {
        return agent.getDsPool(db).getWriteableDs(EMPTY_PATTERN).getConnection();
    }

    /**
     * 从可路由数据服务中寻找指定pattern对应的具体数据库，然后获取一个只写数据库连接. <br>
     * <strong>注意：</strong>使用前请确认pattern对应的数据库确实是可写的
     * 
     * @param db 数据服务名称
     * @return jdbc连接
     * @throws SQLException
     */
    public Connection getWriteConnection(String db, String pattern) throws SQLException {
        return agent.getDsPool(db).getWriteableDs(pattern).getConnection();
    }

    /**
     * 从可路由数据服务中寻找指定pattern对应的具体数据库，然后获取一个可读写的数据库连接. <br>
     * 此方法用于应对高实时性的数据服务，如果你希望数据在写入数据库后立刻可以读取，那么应当考虑使用此方法。 <br>
     * <strong>注意：</strong>使用前请确认pattern对应的数据库确实是可读写的。
     * 
     * @param db 数据服务名称
     * @return jdbc连接
     * @throws SQLException
     */
    public Connection getReadAndWriteConnection(String db, String pattern) throws SQLException {
        return agent.getDsPool(db).getWriteableDs(pattern).getConnection();
    }
}
