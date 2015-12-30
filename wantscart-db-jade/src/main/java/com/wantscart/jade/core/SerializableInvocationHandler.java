package com.wantscart.jade.core;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;

/**
 * User: chuang.zhang
 * Date: 15/10/8
 * Time: 15:01
 */
public class SerializableInvocationHandler implements MethodInterceptor {

    private Class<?> clazz;

    public SerializableInvocationHandler(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Object result = invocation.proceed();
        TableSchema schema = TableSchema.getSchema(clazz);
        TableSchema.Column column = schema.getColumnOnCall(invocation.getMethod());
        if (column != null && column.getGetter().equals(invocation.getMethod()) && column.getSerializer() != null) {
            ProxyFactory proxyFactory = new ProxyFactory(result);
            proxyFactory.addInterface(Serializer.class);
            proxyFactory.addAdvice(new SerializableColumnHandler(column, result));
            result = proxyFactory.getProxy();
        }
        return result;
    }
}
