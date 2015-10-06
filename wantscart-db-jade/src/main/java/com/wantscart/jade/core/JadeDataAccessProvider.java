package com.wantscart.jade.core;

import com.wantscart.jade.provider.DataAccess;
import com.wantscart.jade.provider.DataAccessProvider;
import org.springframework.stereotype.Component;

/**
 * 
 * @author <a href="mailto:zhangtao@techwolf.cn">Kylen Zhang</a>
 * Initial created at 2014年3月11日下午5:35:18
 *
 */
@Component
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
