package com.wantscart.db.zookeeper;

/**
 * 数据监听器.
 * 
 */
public interface DataListener {

    /**
     * 获取znode path.
     * 
     * @return
     */
    String getPath();

    /**
     * 数据发生改变
     */
    void onDataChange();
}
