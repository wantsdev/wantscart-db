package com.wantscart.jade.core;

import com.wantscart.jade.annotation.ReturnGeneratedKeys;
import com.wantscart.jade.annotation.SQL;
import com.wantscart.jade.annotation.SQLParam;
import com.wantscart.jade.annotation.SQLType;

import java.util.List;

/**
 * User: chuang.zhang
 * Date: 15/10/6
 * Time: 13:52
 */
public interface BaseDAO<T> {

    @ReturnGeneratedKeys
    @SQL(value = "INSERT INTO " + TableSchema.TEMPLATE_TABLE + " (" + TableSchema.TEMPLATE_COLUMN_KEYS + ") VALUES (" + TableSchema.TEMPLATE_COLUMN_VALS + ")", type = SQLType.TEMPLATE)
    Number add(@SQLParam("_t") T t);

    @SQL(value = "UPDATE " + TableSchema.TEMPLATE_TABLE + " SET " + TableSchema.TEMPLATE_COLUMN_PAIRS + " WHERE " + TableSchema.TEMPLATE_PK + " = :_id", type = SQLType.TEMPLATE)
    int update(@SQLParam("_id") Number id, @SQLParam("_t") T t);

    @SQL(value = "DELETE FROM " + TableSchema.TEMPLATE_TABLE + " WHERE " + TableSchema.TEMPLATE_PK + " = :_id", type = SQLType.TEMPLATE)
    int delete(@SQLParam("_id") Number id);

    @SQL(value = "SELECT " + TableSchema.TEMPLATE_PK + ", " + TableSchema.TEMPLATE_COLUMN_KEYS + " FROM " + TableSchema.TEMPLATE_TABLE + " WHERE " + TableSchema.TEMPLATE_PK + " = :_id", type = SQLType.TEMPLATE)
    T get(@SQLParam("_id") Number id);

    @SQL(value = "SELECT " + TableSchema.TEMPLATE_PK + ", " + TableSchema.TEMPLATE_COLUMN_KEYS + " FROM " + TableSchema.TEMPLATE_TABLE + " WHERE " + TableSchema.TEMPLATE_PK + " IN (:_ids)", type = SQLType.TEMPLATE)
    List<T> gets(@SQLParam("_ids") List<Number> ids);

    @SQL(value = "SELECT COUNT(" + TableSchema.TEMPLATE_PK + ") FROM " + TableSchema.TEMPLATE_TABLE, type = SQLType.TEMPLATE)
    long count();

    @SQL(value = "SELECT " + TableSchema.TEMPLATE_PK + ", " + TableSchema.TEMPLATE_COLUMN_KEYS + " FROM " + TableSchema.TEMPLATE_TABLE + " ORDER BY " + TableSchema.TEMPLATE_PK + " DESC LIMIT :_offset,:_limit", type = SQLType.TEMPLATE)
    List<T> list(@SQLParam("_offset") long offset, @SQLParam("_limit") int limit);

}
