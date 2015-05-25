/*
 * Copyright 2009-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License i distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tucaohui.jade.core;

import com.tucaohui.jade.provider.DataAccess;
import com.tucaohui.jade.provider.DataAccessProvider;
import com.tucaohui.jade.provider.Definition;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Proxy;

/**
 * 提供 DAO 对象的 Spring-framework {@link FactoryBean} 工厂。
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @author 廖涵 [in355hz@gmail.com]
 * 
 */
public class JadeDaoFactoryBean<T> implements FactoryBean, InitializingBean {

    private T dao;

    private Class<T> daoClass;

    protected DataAccessProvider dataAccessProvider;

    public void setDaoClass(Class<T> daoClass) {
        this.daoClass = daoClass;
    }

    public void setDataAccessProvider(DataAccessProvider dataAccessProvider) {
        this.dataAccessProvider = dataAccessProvider;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(dataAccessProvider);
        Assert.isTrue(daoClass.isInterface(), "not a interface class: " + daoClass.getName());
    }

    @Override
    public T getObject() {
        if (dao == null) {
            synchronized (this) {
                if (dao == null) {
                    dao = createDAO(daoClass);
                }
            }
        }
        Assert.notNull(dao);
        return dao;
    }

    @SuppressWarnings("unchecked")
    protected T createDAO(Class<T> daoClass) {
        Definition definition = new Definition(daoClass);
        DataAccess dataAccess = dataAccessProvider.createDataAccess(daoClass);
        JadeDaoInvocationHandler handler = new JadeDaoInvocationHandler(dataAccess, definition);
        return (T) Proxy.newProxyInstance(ClassUtils.getDefaultClassLoader(),
                new Class[] { daoClass }, handler);
    }

    @Override
    public Class<T> getObjectType() {
        return daoClass;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
