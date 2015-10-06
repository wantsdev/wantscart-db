package com.wantscart.db.zookeeper;

/**
 * zookeeper事件.
 * 
 */
public abstract class ZKEvent {

    /**
     * 处理事件.
     * 
     * @throws Exception
     */
    abstract void run() throws Exception;
}
