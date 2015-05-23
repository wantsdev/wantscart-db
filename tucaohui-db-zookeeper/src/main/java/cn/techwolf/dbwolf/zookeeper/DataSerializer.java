package cn.techwolf.dbwolf.zookeeper;

import cn.techwolf.dbwolf.zookeeper.exception.ZKDataSerializeException;

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
