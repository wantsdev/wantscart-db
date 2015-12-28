package com.wantscart.jade.core.serializer;

import com.wantscart.jade.core.Serializer;
import com.wantscart.jade.core.TableSchema;
import com.wantscart.jade.exql.util.JsonUtil;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.Type;

/**
 * User: chuang.zhang
 * Date: 15/10/8
 * Time: 16:15
 */
public class EntitySerailizer implements Serializer<Object> {

    @Override
    public Object serialize(Object object) {
        if (object != null) {
            Class clazz = object.getClass();
            TableSchema schema = TableSchema.getSchema(clazz);
            try {
                return schema.getPk().getGetter().invoke(object);
            } catch (Exception e) {
                e.printStackTrace();
            }
            object = JsonUtil.toJson(object);
        }
        return object;
    }

    @Override
    public Object deserialize(Object serialized, Type type) {
        Object instance = BeanUtils.instantiateClass((Class<Object>) type);
        TableSchema schema = TableSchema.getSchema((Class<?>) type);
        try {
            schema.getPk().getSetter().invoke(instance, NumberUtils.createNumber(""+serialized));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instance;
    }



    @Override
    public Class columnType() {
        return String.class;
    }
}
