package cn.techwolf.dbwolf.zookeeper;

import cn.techwolf.dbwolf.zookeeper.exception.ZKDataDeserializeException;

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
