package com.wantscart.db.zookeeper;

/**
 * 状态监听器.
 * 
 */
public interface StateListener {

    /**
     * 新连接建立或重连.
     */
    void onNewSession();

    /**
     * 状态发生改变.
     */
    void onStateChange();
}
