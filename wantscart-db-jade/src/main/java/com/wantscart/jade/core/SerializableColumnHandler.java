package com.wantscart.jade.core;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * User: chuang.zhang
 * Date: 15/10/8
 * Time: 15:01
 */
public class SerializableColumnHandler implements MethodInterceptor {

    private TableSchema.Column column;

    private Object object;

    public SerializableColumnHandler(TableSchema.Column column, Object object) {
        this.column = column;
        this.object = object;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Object result = object;
        if (invocation.getMethod().getName().equals("serialize")) {
            result = column.getSerializer().serialize(result);
        }
        return result;
    }
}
