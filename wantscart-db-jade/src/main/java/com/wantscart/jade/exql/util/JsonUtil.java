package com.wantscart.jade.exql.util;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * User: chuang.zhang
 * Date: 15/10/8
 * Time: 16:23
 */
public class JsonUtil {

    private static final Log logger = LogFactory.getLog(JsonUtil.class);

    private static final GsonBuilder builder = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").registerTypeAdapter(AtomicLong.class, new AtomicLongTypeAdapter());

    private static final Gson UDSGSON = builder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    private static final Gson IDGSON = builder.setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create();

    private static final int TID = 0;

    private static final int TUDS = 1;

    public static final Type TYPE_LIST_STR = new TypeToken<List<String>>() {
    }.getType();

    public static final Type TYPE_LIST_INT = new TypeToken<List<Integer>>() {
    }.getType();

    public static final Type TYPE_LIST_LONG = new TypeToken<List<Long>>() {
    }.getType();

    protected static Gson getGson(int type){
        Gson gson;
        switch (type){
            case TUDS:
                gson = UDSGSON;
                break;
            default:
                gson = IDGSON;
        }
        return gson;
    }

    public static String toJson(Object o, int type) {
        return getGson(type).toJson(o);
    }

    public static String toJson(Object o) {
        return toJson(o, TID);
    }

    public static <T> T fromJson(String json, Class<T> t, int type) {
        try {
            return getGson(type).fromJson(json, t);
        } catch (Exception e) {
            logger.debug("json decode error!" + json);
            return null;
        }
    }

    public static <T> T fromJson(String json, Type t, int type) {
        try {
            return getGson(type).fromJson(json, t);
        } catch (Exception e) {
            logger.debug("json decode error!" + json);
            return null;
        }
    }

    public static <T> T fromJson(String json, Class<T> t) {
        return fromJson(json, t, TID);
    }

    public static <T> T fromJson(String json, Type t) {
        return fromJson(json, t, TID);
    }

    static class AtomicLongTypeAdapter implements JsonSerializer {

        @Override
        public JsonElement serialize(Object src, Type typeOfSrc, JsonSerializationContext context) {
            if (src instanceof AtomicLong) {
                JsonElement je = new JsonPrimitive(((AtomicLong) src).get() + "");
                return je;
            }
            return null;
        }
    }

    static class DateTypeAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {

        @Override
        public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
            JsonElement je = new JsonPrimitive(src.getTime());
            return je;
        }

        @Override
        public java.util.Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new Date(json.getAsLong());
        }
    }
}
