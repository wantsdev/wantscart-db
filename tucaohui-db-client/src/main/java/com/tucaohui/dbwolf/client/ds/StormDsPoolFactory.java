package com.tucaohui.dbwolf.client.ds;

import com.tucaohui.dbwolf.client.DbAgent;
import com.tucaohui.dbwolf.xml.DbInstanceConfig;

/**
 * dbwolf数据源工厂，根据配置类型来创建路由数据源或单实例数据源。
 * 
 */
public class StormDsPoolFactory {

    public static StormDataSourcePool createStormDs(final DbAgent agent,
            final DbInstanceConfig config) {
        StormDataSourcePool ds = null;
        switch (config.getType()) {
            case CDbInstanceSingler:
                ds = new SinglerDsPool(config);
                break;
            case CDbInstanceRouter:
                ds = new RouterDsPool(agent, config);
                break;
            default:
                throw new IllegalArgumentException("unknown instance type:" + config.getType());
        }
        return ds;
    }
}
