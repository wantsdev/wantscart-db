package com.wantscart.jade.annotation;

import com.wantscart.jade.core.Serializer;
import com.wantscart.jade.core.serializer.DefaultJsonSerailizer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * User: chuang.zhang
 * Date: 15/10/7
 * Time: 16:31
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Serializable {
    Class<? extends Serializer> serialzer() default DefaultJsonSerailizer.class;
}
