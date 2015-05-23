package com.tucaohui.dbwolf.zookeeper;

/**
 * 节点监听器.
 * 
 */
public interface NodeListener {

    /**
     * 当数据库配置发生改变后回调.
     */
    void onNodeChange();

    /**
     * 获取znode path.
     * 
     * @return
     */
    String getPath();
}
