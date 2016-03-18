package com.wantscart.jade.core.serializer;

import com.wantscart.jade.core.Serializer;
import com.wantscart.jade.exql.util.JsonUtil;

import java.lang.reflect.Type;

/**
 * User: chuang.zhang
 * Date: 15/10/8
 * Time: 16:15
 */
public class JsonSerailizer implements Serializer<Object> {

    @Override
    public Object serialize(Object object) {
        if (object != null) {
            object = JsonUtil.toJson(object);
        }
        return object;
    }

    @Override
    public Object deserialize(Object serialized, Type type) {
        Object object = serialized;
        if (object != null) {
            object = JsonUtil.fromJson(object.toString(), type);
        }
        return object;
    }

    @Override
    public Class columnType() {
        return String.class;
    }
}
