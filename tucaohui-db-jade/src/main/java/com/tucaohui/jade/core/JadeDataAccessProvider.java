package com.tucaohui.jade.core;

import com.tucaohui.jade.provider.DataAccess;
import com.tucaohui.jade.provider.DataAccessProvider;

/**
 * 
 * @author <a href="mailto:zhangtao@techwolf.cn">Kylen Zhang</a>
 * Initial created at 2014年3月11日下午5:35:18
 *
 */
public class JadeDataAccessProvider implements DataAccessProvider {

    private DataAccessProvider targetAccessProvider;

    public void setTargetAccessProvider(DataAccessProvider targetAccessProvider) {
        this.targetAccessProvider = targetAccessProvider;
    }

    @Override
    public DataAccess createDataAccess(Class<?> daoClass) {
        DataAccess dataAccess = targetAccessProvider.createDataAccess(daoClass);
        dataAccess = new SQLThreadLocalWrapper(dataAccess);
        return dataAccess;
    }
}
