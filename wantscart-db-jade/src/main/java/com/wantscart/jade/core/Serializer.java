package com.wantscart.jade.core;

import org.springframework.beans.BeanUtils;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: chuang.zhang
 * Date: 15/10/7
 * Time: 16:33
 */
public interface Serializer<T> {

    public Object serialize(T object);

    public T deserialize(Object serialized, Type type);

    public Class columnType();

    static class Provider {

        private static final Map<Class<? extends Serializer>, Serializer> cache = new ConcurrentHashMap<Class<? extends Serializer>, Serializer>();

        public static Serializer getSerizlizer(Class<? extends Serializer> clazz) {
            Serializer s = cache.get(clazz);
            if (s == null) {
                s = BeanUtils.instantiate(clazz);
                cache.put(clazz, s);
            }
            return s;
        }
    }
}
