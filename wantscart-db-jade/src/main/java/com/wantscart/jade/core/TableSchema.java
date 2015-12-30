package com.wantscart.jade.core;

import com.wantscart.jade.annotation.Alias;
import com.wantscart.jade.annotation.JadeExpose;
import com.wantscart.jade.annotation.PrimaryKey;
import com.wantscart.jade.annotation.Serializable;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.reflect.FieldUtils;
import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: chuang.zhang
 * Date: 15/10/6
 * Time: 17:03
 */
public class TableSchema {

    public static final String TEMPLATE_TABLE = ":TABLE";

    public static final String TEMPLATE_PK = ":PK";

    public static final String TEMPLATE_COLUMN_KEYS = ":COLUMN_KEYS";

    public static final String TEMPLATE_COLUMN_VALS = ":COLUMN_VALS";

    public static final String TEMPLATE_COLUMN_PAIRS = ":COLUMN_PAIRS";

    public static final String TEMPLATE_COLUMN_ALL = TEMPLATE_PK + ", " + TEMPLATE_COLUMN_KEYS;

    public static final String[] ALL_TEMPLATE = new String[]{TEMPLATE_TABLE, TEMPLATE_PK, TEMPLATE_COLUMN_KEYS, TEMPLATE_COLUMN_VALS, TEMPLATE_COLUMN_PAIRS};

    private static final Map<Class, TableSchema> schemas = new ConcurrentHashMap<Class, TableSchema>();

    private Map<Method, Column> methodCallHoder = new ConcurrentHashMap<Method, Column>();

    private Map<String, String> templateHolder = new ConcurrentHashMap<String, String>();

    private String tableName;

    private Columns columns;

    private Column pk;

    private TableSchema() {
        this.columns = new Columns();
    }

    public Column getColumnOnCall(Method m) {
        Column callColumn = methodCallHoder.get(m);
        if (callColumn == null) {
            for (Column column : columns) {
                if ((column.getSetter() != null && column.getSetter().equals(m)) || (column.getGetter() != null && column.getGetter().equals(m))) {
                    methodCallHoder.put(m, column);
                    callColumn = column;
                    break;
                }
            }
        }
        return callColumn;
    }

    public String getByTemplate(String templateName) {
        String val = templateHolder.get(templateName);
        if (StringUtils.isBlank(val)) {
            if (TEMPLATE_TABLE.equals(templateName)) {
                val = getTableName();
            } else if (TEMPLATE_PK.equals(templateName)) {
                val = getPk().getName();
            } else if (TEMPLATE_COLUMN_KEYS.equals(templateName)) {
                StringBuilder sb = new StringBuilder();
                Iterator<Column> itr = columns.iterator();
                while (itr.hasNext()) {
                    Column column = itr.next();
                    sb.append(column.getName());
                    if (itr.hasNext()) {
                        sb.append(", ");
                    }
                }
                val = sb.toString();
            } else if (TEMPLATE_COLUMN_VALS.equals(templateName)) {
                StringBuilder sb = new StringBuilder();
                Iterator<Column> itr = columns.iterator();
                while (itr.hasNext()) {
                    Column column = itr.next();
                    sb.append(":_t.").append(column.getOriginName());
                    if (itr.hasNext()) {
                        sb.append(", ");
                    }
                }
                val = sb.toString();
            } else if (TEMPLATE_COLUMN_PAIRS.equals(templateName)) {
                StringBuilder sb = new StringBuilder();
                Iterator<Column> itr = columns.iterator();
                while (itr.hasNext()) {
                    Column column = itr.next();
                    sb.append(column.getName());
                    sb.append("=");
                    sb.append(":_t.").append(column.getOriginName());
                    if (itr.hasNext()) {
                        sb.append(", ");
                    }
                }
                val = sb.toString();
            }
            templateHolder.put(templateName, val);
        }
        return val;
    }

    public static TableSchema getSchema(Class<?> type) {
        if (!schemas.containsKey(type)) {
            TableSchema schema = new TableSchema();
            Alias tableName = type.getAnnotation(Alias.class);
            schema.setTableName(tableName != null ? tableName.value() : translate(type.getSimpleName()));
            PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(type);
            for (PropertyDescriptor pd : pds) {
                if (pd.getName().equals("class") || pd.getWriteMethod() == null || pd.getReadMethod() == null) {
                    continue;
                }
                Field f = null;


                for (Class<?> clazz = type; clazz != Object.class; clazz = clazz.getSuperclass()) {
                    try {
                        f = FieldUtils.getDeclaredField(clazz, pd.getName(), true);
                        if(f != null){
                            break;
                        }
                    } catch (Exception e) {
                        //do nothing
                    }
                }

                if (f == null) {
                    //todo log warning
                    continue;
                }

                //set pk
                if ((f.isAnnotationPresent(PrimaryKey.class) || pd.getName().equals("id")) && (pd.getPropertyType().isAssignableFrom(Number.class) || pd.getPropertyType().isPrimitive())) {
                    Column pk = new Column();
                    pk.setName(pd.getName());
                    pk.setOriginName(pd.getName());
                    if (f.isAnnotationPresent(Alias.class)) {
                        Alias alias = f.getAnnotation(Alias.class);
                        pk.setName(alias.value());
                    }
                    pk.setGetter(pd.getReadMethod());
                    pk.setSetter(pd.getWriteMethod());
                    pk.setPk(true);
                    schema.setPk(pk);
                } else {
                    f.setAccessible(true);
                    Column column = new Column();
                    if (f.isAnnotationPresent(JadeExpose.class)) {
                        continue;
                    }
                    column.setName(translate(pd.getName()));
                    column.setOriginName(pd.getName());
                    if (f.isAnnotationPresent(Alias.class)) {
                        Alias alias = f.getAnnotation(Alias.class);
                        column.setName(alias.value());
                    }
                    if (f.isAnnotationPresent(Serializable.class)) {
                        Serializable serializable = f.getAnnotation(Serializable.class);
                        column.setSerializer(Serializer.Provider.getSerizlizer(serializable.serialzer()));
                    }
                    column.setSetter(pd.getWriteMethod());
                    column.setGetter(pd.getReadMethod());
                    column.setType(pd.getPropertyType());
                    column.setPk(false);
                    schema.columns.add(column);
                }
            }
            schemas.put(type, schema);
        }
        return schemas.get(type);
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Columns getColumns() {
        return columns;
    }

    public void setColumns(Columns columns) {
        this.columns = columns;
    }

    public Column getPk() {
        return pk;
    }

    public void setPk(Column pk) {
        this.pk = pk;
    }

    public static class Column {

        private String name;

        private String originName;

        private Type type;

        private Method getter;

        private Method setter;

        private Serializer serializer;

        private boolean pk;

        private boolean baseType = false;

        public Method getGetter() {
            return getter;
        }

        public void setGetter(Method getter) {
            this.getter = getter;
        }

        public Method getSetter() {
            return setter;
        }

        public void setSetter(Method setter) {
            this.setter = setter;
        }

        public Type getType() {
            return type;
        }

        public void setType(Type type) {
            this.type = type;
            setBaseType(TypeUtils.isBaseType((Class) type));
        }

        public boolean isBaseType() {
            return baseType;
        }

        protected void setBaseType(boolean baseType) {
            this.baseType = baseType;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isPk() {
            return pk;
        }

        public void setPk(boolean pk) {
            this.pk = pk;
        }

        public String getOriginName() {
            return originName;
        }

        public void setOriginName(String originName) {
            this.originName = originName;
        }

        public Serializer getSerializer() {
            return serializer;
        }

        public void setSerializer(Serializer serializer) {
            this.serializer = serializer;
        }

        public Type getColumnType() {
            if (serializer != null) {
                return serializer.columnType();
            }
            return type;
        }
    }

    public static class Columns extends LinkedList<Column> {


    }

    public static String translate(String input) {
        if (input == null) return input; // garbage in, garbage out
        int length = input.length();
        StringBuilder result = new StringBuilder(length * 2);
        int resultLength = 0;
        boolean wasPrevTranslated = false;
        for (int i = 0; i < length; i++) {
            char c = input.charAt(i);
            if (i > 0 || c != '_') // skip first starting underscore
            {
                if (Character.isUpperCase(c)) {
                    if (!wasPrevTranslated && resultLength > 0 && result.charAt(resultLength - 1) != '_') {
                        result.append('_');
                        resultLength++;
                    }
                    c = Character.toLowerCase(c);
                    wasPrevTranslated = true;
                } else {
                    wasPrevTranslated = false;
                }
                result.append(c);
                resultLength++;
            }
        }
        return resultLength > 0 ? result.toString() : input;
    }

}
