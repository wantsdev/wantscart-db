package com.tucaohui.jade.datasource.router;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.tucaohui.jade.datasource.Router;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.BadSqlGrammarException;

/**
 * 实现: HASH 算法进行散表的配置记录。
 * 
 * @author han.liao
 */
public class DateRouter implements Router {

    // 输出日志
    protected static final Log logger = LogFactory.getLog(DateRouter.class);

    protected String column, pattern, partitions;

    private boolean excludeNow = false;

    /**
     * 创建配置记录。
     * 
     * @param column - 配置的列
     * @param pattern - 数据表的名称模板
     * @param partitions - 分区表达式如 yyyyddMM
     */
    public DateRouter(String column, String pattern, String partitions, boolean excludeNow) {
        if (pattern == null) {
            throw new NullPointerException("pattern");
        }
        if (partitions == null) {
            throw new NullPointerException("partitions");
        }
        this.column = column;
        this.pattern = pattern;
        this.partitions = partitions;
        this.excludeNow = excludeNow;
    }

    @Override
    public String getColumn() {
        return column;
    }

    /**
     * 设置配置的列。
     * 
     * @param column - 配置的列
     */
    public void setColumn(String column) {
        this.column = column;
    }

    /**
     * 返回数据表的名称模板。
     * 
     * @return 数据表的名称模板
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * 设置数据表的名称模板。
     * 
     * @param pattern - 数据表的名称模板
     */
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getPartitions() {
        return partitions;
    }

    public void setPartitions(String partitions) {
        this.partitions = partitions;
    }

    @Override
    public String doRoute(Object columnValue) {

        if (pattern != null && columnValue != null) {
            if (columnValue.getClass() == String.class) {
                if (org.apache.commons.lang.StringUtils.isNumeric((String) columnValue)) {
                    columnValue = Long.parseLong((String) columnValue);
                }
            }
            Date date;
            if (columnValue instanceof Date) {
                date = (Date) columnValue;
            } else if (columnValue.getClass() == Long.class) {
                date = new Date((Long) columnValue);
            } else if (columnValue.getClass() == Calendar.class) {
                Calendar ca = (Calendar) columnValue;
                date = ca.getTime();
            } else {
                throw new IllegalArgumentException("wrong date type '"
                        + columnValue.getClass().getName() + "' of value: " + columnValue);
            }
            SimpleDateFormat sdf = new SimpleDateFormat(this.partitions);
            String _partitions = sdf.format(date);
            String name = null;
            if (excludeNow) {
                String _now = sdf.format(new Date());
                if (_now.equals(_partitions)) {
                    _partitions = null;
                }
            }
            if (_partitions != null) {
                name = MessageFormat.format(pattern, _partitions);
            }

            // 输出日志
            if (logger.isDebugEnabled()) {
                logger.debug("Routing on [" + column + " = " + columnValue + ", "
                        + columnValue.getClass() + "]: " + name);
            }

            return name;
        } else {
            throw new IllegalArgumentException("pattern=" + pattern + "; columnValue="
                    + columnValue);
        }

    }

    protected long convert(Object columnValue) {

        if (columnValue instanceof Number) {

            return ((Number) columnValue).longValue();

        } else {

            try {
                // 转换成字符串处理
                return Long.parseLong(String.valueOf(columnValue));

            } catch (NumberFormatException e) {

                // 输出日志
                if (logger.isWarnEnabled()) {
                    logger.warn("Column \'" + column // NL
                            + "\' must be number, but: " + columnValue);
                }

                throw new BadSqlGrammarException("HashRouter.convert", "Column \'" + column // NL
                        + "\' must be number, but: " + columnValue, null);
            }
        }
    }
}
