package com.wantscart.jade.core;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: chuang.zhang
 * Date: 15/10/8
 * Time: 15:01
 */
public class SerializableInvocationHandler implements MethodInterceptor {

    private Map<Method, Boolean> sCache = new ConcurrentHashMap<Method, Boolean>();

    private Class<?> clazz;

    public SerializableInvocationHandler(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        Object proxy = invocation.getThis();
        Object[] args = invocation.getArguments();
        Object result = method.invoke(proxy, args);
        if (!sCache.containsKey(method)) {
            TableSchema schema = TableSchema.getSchema(clazz);
            TableSchema.Column column = schema.getColumnOnCall(method);
            sCache.put(method, column.getSerializer() != null);
        }
        if (sCache.get(method)) {
            TableSchema schema = TableSchema.getSchema(clazz);
            TableSchema.Column column = schema.getColumnOnCall(method);
            Serializer serializer = column.getSerializer();
            if (method.equals(column.getGetter())) {
                result = serializer.serialize(result);
            } else if (method.equals(column.getSetter())) {
                result = serializer.deserialize(result, column.getType());
            }
        }
        return result;
    }
}
