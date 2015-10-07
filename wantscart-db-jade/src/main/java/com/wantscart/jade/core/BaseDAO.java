package com.wantscart.jade.core;

import com.wantscart.jade.annotation.*;

/**
 * User: chuang.zhang
 * Date: 15/10/6
 * Time: 13:52
 */
public interface BaseDAO<T> {

    @ReturnGeneratedKeys
    @SQL(value = "INSERT INTO " + TableSchema.TEMPLATE_TABLE + " (" + TableSchema.TEMPLATE_COLUMN_KEYS + ") VALUES (" + TableSchema.TEMPLATE_COLUMN_VALS + ")", type = SQLType.TEMPLATE)
    public Number add(@SQLParam("_t") T t);

    @SQL(value = "UPDATE " + TableSchema.TEMPLATE_TABLE + " SET " + TableSchema.TEMPLATE_COLUMN_PAIRS + " WHERE " + TableSchema.TEMPLATE_PK + " = :_id", type = SQLType.TEMPLATE)
    public int update(@SQLParam("_id") Number id, @SQLParam("_t") T t);

    @SQL(value = "DELETE " + TableSchema.TEMPLATE_TABLE + " WHERE " + TableSchema.TEMPLATE_PK + " = :_id", type = SQLType.TEMPLATE)
    public int delete(@SQLParam("_id") Number id);

    @SQL(value = "SELECT " + TableSchema.TEMPLATE_PK + ", " + TableSchema.TEMPLATE_COLUMN_KEYS + " FROM " + TableSchema.TEMPLATE_TABLE + " WHERE " + TableSchema.TEMPLATE_PK + " = :_id", type = SQLType.TEMPLATE)
    public T get(@SQLParam("_id") Number id);
}
