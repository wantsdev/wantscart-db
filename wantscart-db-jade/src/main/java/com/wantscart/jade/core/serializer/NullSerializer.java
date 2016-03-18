package com.wantscart.jade.core.serializer;

import com.wantscart.jade.core.Serializer;

import java.lang.reflect.Type;

/**
 * User: chuang.zhang
 * Date: 16/3/18
 * Time: 17:22
 */
public class NullSerializer implements Serializer {

    @Override
    public Object serialize(Object object) {
        return null;
    }

    @Override
    public Object deserialize(Object serialized, Type type) {
        return null;
    }

    @Override
    public Class columnType() {
        return null;
    }
}
