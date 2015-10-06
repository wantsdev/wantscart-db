package com.wantscart.db.zookeeper;

import com.wantscart.db.zookeeper.exception.ZKDataDeserializeException;

/**
 * 对象反序列化.
 * 
 * @param <T>
 */
public interface DataDeserializer<T> {

    /**
     * 反序列化.
     * 
     * @param data
     * @return
     * @throws ZKDataDeserializeException
     */
    T deserialize(byte[] data) throws ZKDataDeserializeException;
}
