package com.wantscart.jade.core;

/**
 * User: chuang.zhang
 * Date: 15/10/7
 * Time: 16:33
 */
public interface Serializable {

    Object serialize();

    void deserialize(Object serialized);
}
