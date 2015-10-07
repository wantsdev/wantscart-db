package com.wantscart.jade.core;

import org.springframework.util.ClassUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.util.Date;

public class TypeUtils {

    public static boolean isColumnType(Class<?> columnTypeCandidate) {
        return String.class == columnTypeCandidate // NL
                || ClassUtils.isPrimitiveOrWrapper(columnTypeCandidate)// NL
                || Date.class.isAssignableFrom(columnTypeCandidate) // NL
                || columnTypeCandidate == byte[].class // NL
                || columnTypeCandidate == BigDecimal.class // NL
                || columnTypeCandidate == Blob.class // NL
                || columnTypeCandidate == Clob.class;
    }

    public static boolean isBaseType(Class clazz) {
        return clazz.equals(String.class) ||
                clazz.equals(Integer.class) ||
                clazz.equals(Byte.class) ||
                clazz.equals(Long.class) ||
                clazz.equals(Double.class) ||
                clazz.equals(Float.class) ||
                clazz.equals(Character.class) ||
                clazz.equals(Short.class) ||
                clazz.equals(BigDecimal.class) ||
                clazz.equals(BigInteger.class) ||
                clazz.equals(Boolean.class) ||
                clazz.equals(Date.class) ||
                clazz.isPrimitive();
    }
}
