package com.wantscart.jade.core.serializer;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * User: chuang.zhang
 * Date: 15/10/8
 * Time: 16:15
 */
public class EntityListSerailizer extends EntitySerailizer {

    private static final String SPLITOR = "|";

    @Override
    public Object serialize(Object object) {
        if (object != null && List.class.isAssignableFrom(object.getClass())) {
            StringBuilder sb = new StringBuilder();
            List list = (List) object;
            if (!CollectionUtils.isEmpty(list)) {
                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    Object obj = itr.next();
                    sb.append(super.serialize(obj));
                    if (itr.hasNext()) {
                        sb.append(SPLITOR);
                    }
                }
            }
            return sb.toString();
        }
        return object;
    }

    @Override
    public Object deserialize(Object serialized, Type type) {
        List list = new ArrayList();
        if(type instanceof ParameterizedType){
            type = ((ParameterizedType) type).getActualTypeArguments()[0];
        }
        if (serialized != null) {
            String[] parts = StringUtils.split(serialized.toString(), SPLITOR);
            for (String part : parts) {
                list.add(super.deserialize(part, type));
            }
        }
        return list;
    }


    @Override
    public Class columnType() {
        return String.class;
    }
}
