package com.wantscart.db.zookeeper;

import com.wantscart.db.zookeeper.exception.ZKDataSerializeException;

/**
 * 对象序列化。
 * 
 * @param <T>
 */
public interface DataSerializer<T> {

    /**
     * 序列化.
     * 
     * @param obj
     * @return
     * @throws ZKDataSerializeException
     */
    byte[] serialize(final T obj) throws ZKDataSerializeException;

}
