package com.wantscart.jade.core.serializer;

import com.wantscart.jade.core.Serializer;

import java.lang.reflect.Type;
import java.util.List;

/**
 * User: chuang.zhang
 * Date: 15/10/8
 * Time: 14:55
 */
public class ListSerializer<F> implements Serializer<List> {

    @Override
    public Object serialize(List object) {
        return null;
    }

    @Override
    public List deserialize(Object serialized, Type type) {
        return null;
    }
}
