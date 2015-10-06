package com.wantscart.jade.provider.jdbc;

import com.wantscart.jade.exql.ExqlPattern;
import com.wantscart.jade.exql.impl.ExqlContextImpl;
import com.wantscart.jade.exql.impl.ExqlPatternImpl;
import com.wantscart.jade.provider.DataAccess;
import com.wantscart.jade.provider.Modifier;
import com.wantscart.jade.provider.SQLInterpreter;
import com.wantscart.jade.provider.SQLInterpreterResult;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLSyntaxErrorException;
import java.util.Map;

/**
 * 提供动态: SQL 语句功能的 {@link DataAccess} 实现。
 * 
 * @author 廖涵 [in355hz@gmail.com]
 */
@Component
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
