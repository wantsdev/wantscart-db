package com.tucaohui.dbwolf.zookeeper;

import com.tucaohui.dbwolf.zookeeper.exception.ZKDataDeserializeException;

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
     * @throws com.tucaohui.dbwolf.zookeeper.exception.ZKDataDeserializeException
     */
    T deserialize(byte[] data) throws ZKDataDeserializeException;
}
