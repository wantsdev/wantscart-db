package com.tucaohui.jade.provider.jdbc;

import java.sql.SQLSyntaxErrorException;
import java.util.Map;

import javax.sql.DataSource;

import com.tucaohui.jade.exql.impl.ExqlContextImpl;
import com.tucaohui.jade.exql.impl.ExqlPatternImpl;
import com.tucaohui.jade.provider.Modifier;
import org.springframework.jdbc.BadSqlGrammarException;

import com.tucaohui.jade.exql.ExqlPattern;
import com.tucaohui.jade.provider.DataAccess;
import com.tucaohui.jade.provider.SQLInterpreter;
import com.tucaohui.jade.provider.SQLInterpreterResult;

/**
 * 提供动态: SQL 语句功能的 {@link DataAccess} 实现。
 * 
 * @author 廖涵 [in355hz@gmail.com]
 */
public class SimpleSQLInterpreter implements SQLInterpreter {

    @Override
    // 转换 JadeSQL 语句为正常的 SQL 语句
    public SQLInterpreterResult interpret(DataSource dataSource, String sql, Modifier modifier,
            Map<String, Object> parametersAsMap, Object[] parametersAsArray) {

        // 转换语句中的表达式
        ExqlPattern pattern = ExqlPatternImpl.compile(sql);
        ExqlContextImpl context = new ExqlContextImpl(sql.length() + 32);

        try {
            pattern.execute(context, parametersAsMap, modifier.getDefinition().getConstants());

        } catch (Exception e) {
            String daoInfo = modifier.toString();
            throw new BadSqlGrammarException(daoInfo, sql, new SQLSyntaxErrorException(daoInfo
                    + " @SQL('" + sql + "')", e));
        }
        return context;
    }

}
