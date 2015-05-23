package com.tucaohui.dbwolf.client.ds;

import javax.sql.DataSource;

/**
 * 经过封装的数据源。此数据源不是jdbc意义上的.
 * 
 */
public interface StormDataSourcePool {

    /**
     * 获取一个只写数据源.
     * 
     * @param pattern 匹配模式，正则形式
     * @return 数据源实例
     */
    public DataSource getWriteableDs(final String pattern);

    /**
     * 获取一个只读数据源.
     * 
     * @param pattern 匹配模式，正则形式
     * @return 数据源实例
     */
    public DataSource getReadableDs(final String pattern);

    /**
     * 获取数据源配置时间戳.
     * 
     * @return 时间戳
     */
    public long getTimeStamp();

    /**
     * 关闭数据源.
     */
    public void close();
}
