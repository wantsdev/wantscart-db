package com.tucaohui.jade.datasource;

import com.tucaohui.dbwolf.parser.dbobject.Column;
import com.tucaohui.dbwolf.parser.dbobject.Table;
import com.tucaohui.jade.core.SQLThreadLocal;
import com.tucaohui.jade.provider.Modifier;
import com.tucaohui.jade.provider.SQLInterpreter;
import com.tucaohui.jade.provider.SQLInterpreterResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.datasource.DelegatingDataSource;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.util.Map;

// 按Spring语义规定，Order值越高该解释器越后执行
@Order(9000)
public class RouterInterpreter implements SQLInterpreter {

    private static final Log logger = LogFactory.getLog(RouterInterpreter.class);

    // 配置信息
    protected RoutingConfigurator routingConfigurator;

    public void setConfigurator(RoutingConfigurator routingConfigurator) {
        this.routingConfigurator = routingConfigurator;
    }

    public SQLInterpreterResult interpret(DataSource dataSource, final String sql,
            Modifier modifier, Map<String, Object> parametersAsMap, final Object[] parametersAsArray) {
        if (dataSource instanceof DelegatingDataSource) {
            dataSource = ((DelegatingDataSource) dataSource).getTargetDataSource();
        }
        if (!(dataSource instanceof XnDataSource)) {
            return null;
        }
        Assert.notNull(parametersAsArray,
                "need parametersAsArray prepared before invoking this interpreter!");
        String bizName = ((XnDataSource) dataSource).getBizName();
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking analyzing: " + sql);
        }
        SQLParseInfo parseInfo = SQLParseInfo.getParseInfo(sql);
        // 从查询的数据表获取路由配置。
        Table[] tables = parseInfo.getTables();
        //        if (tables == null || tables.length == 0) {
        //            throw new BadSqlGrammarException("jade-sql-parsing", sql, new SQLException(
        //                    "table not found"));
        //        }

        RoutingInfo routingInfo = null;
        //
        if (tables != null) {
            int beginIndex = 0;
            if (parseInfo.isInsert() && tables.length > 1) {
                // INSERT ... SELECT 查询
                beginIndex = 1;
            }

            // 查找散表配置
            for (int i = beginIndex; i < tables.length; i++) {
                RoutingDescriptor descriptor = routingConfigurator.getDescriptor(bizName,
                        tables[i].getName());
                if (descriptor != null) {
                    routingInfo = new RoutingInfo(tables[i], descriptor);
                    break;
                }
            }
        }
        if (routingInfo == null) {
            return null;
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Find routing info: " + routingInfo.byTable + ", "
                        + routingInfo.getDbRouterColumn());
            }
        }
        String forwardTableName = null;
        String forwardDbPattern = null;

        if (routingInfo.getTableRouter() != null) {

            // 用语句信息的常量进行散表。
            Column column = routingInfo.getTableRouterColumn();
            Object columnValue = null;

            if (column != null) {
                columnValue = findShardParamValue(parseInfo, column, parametersAsMap,
                        parametersAsArray);
                if (columnValue == null) {
                    throw new BadSqlGrammarException("sharding", parseInfo.getSQL(), null);
                }
            }

            // 获得散表的名称
            forwardTableName = routingInfo.getTableRouter().doRoute(columnValue);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("table router is null for sql \"" + sql + "\"");
            }
        }

        if (routingInfo.getDbRouter() != null) {

            // 用语句信息的常量进行散库。
            Column column = routingInfo.getDbRouterColumn();
            Object columnValue = null;

            if (column != null) {
                columnValue = findShardParamValue(parseInfo, column, parametersAsMap,
                        parametersAsArray);
                if (columnValue == null) {
                    throw new BadSqlGrammarException("sharding", parseInfo.getSQL(), null);
                }
            }

            // 获得散库的名称
            forwardDbPattern = routingInfo.getDbRouter().doRoute(columnValue);
            if (forwardDbPattern != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("db pattern is '" + forwardDbPattern + "'");
                }
                parametersAsMap.put(XnDataSource.DB_PATTERN, forwardDbPattern);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("db pattern is empty");
                }
                parametersAsMap.put(XnDataSource.DB_PATTERN, XnDataSource.EMPTY_PATTERN);
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("db router is null for sql \"" + sql + "\"");
            }
        }
        //
        String byTableName = routingInfo.byTable.getName();
        final String sqlRewrited;
        if ((forwardTableName != null) && !forwardTableName.equals(byTableName)) {

            // 使用  SqlRewriter 拆分语句，进行所需的查找和替换。
            sqlRewrited = SqlRewriter.rewriteSqlTable(sql, byTableName, forwardTableName);

            // 输出重写日志
            if (logger.isDebugEnabled()) {
                logger.debug("Rewriting SQL: \n  From: " + sql + "\n  To:   " + sqlRewrited);
            }
        } else {
            sqlRewrited = sql;
        }
        return new RouterSQLInterpreterResult(forwardDbPattern, sqlRewrited, parametersAsArray);
    }

    class RoutingInfo {

        private Table byTable;

        private RoutingDescriptor descriptor;

        public RoutingInfo(Table table, RoutingDescriptor descriptor) {
            this.byTable = table;
            this.descriptor = descriptor;
        }

        public Router getDbRouter() {
            return descriptor.getDbRouter();
        }

        public Router getTableRouter() {
            return descriptor.getTableRouter();
        }

        private Column dbRouterColumn;

        public Column getDbRouterColumn() {
            if (dbRouterColumn != null) {
                return dbRouterColumn;
            }
            Router dbRouter = getDbRouter();
            if (dbRouter == null) {
                return null;
            }

            String columnName = dbRouter.getColumn();

            if (columnName != null) {

                // 保存匹配的数据列
                Column columnForDBPartition = new Column();
                columnForDBPartition.setName(columnName.toUpperCase());
                columnForDBPartition.setTable(byTable);
                this.dbRouterColumn = columnForDBPartition;
            }
            return dbRouterColumn;
        }

        private Column tableRouterColumn;

        public Column getTableRouterColumn() {
            if (tableRouterColumn != null) {
                return tableRouterColumn;
            }
            Router tableRouter = getTableRouter();
            if (tableRouter == null) {
                return null;
            }

            String columnName = tableRouter.getColumn();

            if (columnName != null) {

                // 保存匹配的数据列
                Column tableRouterColumn = new Column();
                tableRouterColumn.setName(columnName.toUpperCase());
                tableRouterColumn.setTable(byTable);
                this.tableRouterColumn = tableRouterColumn;
            }
            return tableRouterColumn;
        }
    }

    // 查找散表参数
    protected static Object findShardParamValue(SQLParseInfo parseInfo, Column column,
            Map<String, Object> parametersAsMap, Object[] parametersAsArray) {

        SQLThreadLocal local = SQLThreadLocal.get();

        String shardBy = local.getModifier().getShardBy();
        Object value = null;
        if (shardBy != null) {
            value = parametersAsMap.get(shardBy);
            if (logger.isDebugEnabled()) {
                logger.debug("get shard param value '" + value + "' by @ShardBy (" + shardBy + ")");
            }
            return value;
        } else {
            if (parseInfo.containsParam(column)) {
                // 获取语句中的散表参数
                value = parseInfo.getParam(column);
                if (value != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("find shard param value '" + value + "' by column "
                                + column.getName() + " [constants]");
                    }
                }
                if (value == null) {
                    int index = parseInfo.getColumnIndex(column);
                    if (index >= 0 && index < parametersAsArray.length) {
                        value = parametersAsArray[index];
                        if (logger.isDebugEnabled()) {
                            logger.debug("find shard param value '" + value + "' by column's arg "
                                    + column.getName() + " [index=" + index + " (beginwiths 0)]");
                        }
                    } else {
                        // 如果针对该列进行散表，则必须包含该列作为查询条件。
                        throw new BadSqlGrammarException("interpreter.findShardParamValue", "SQL ["
                                + parseInfo.getSQL() + "] Query without shard parameter: " // NL
                                + column.getSql(), null);
                    }

                }
                return value;
            } else {

                // 如果针对该列进行散表，则必须包含该列作为查询条件。
                throw new BadSqlGrammarException("interpreter.findShardParamValue", "SQL ["
                        + parseInfo.getSQL() + "] Query without shard parameter: " // NL
                        + column.getSql(), null);
            }
        }
    }
}
