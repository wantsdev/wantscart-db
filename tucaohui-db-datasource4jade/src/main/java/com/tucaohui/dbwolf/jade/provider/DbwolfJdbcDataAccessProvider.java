/**
 * $id$
 * Copyright 2014 Techwolf. All rights reserved.
 */
package com.tucaohui.dbwolf.jade.provider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.techwolf.jade.provider.SQLInterpreter;
import cn.techwolf.jade.provider.jdbc.JdbcDataAccessProvider;

/**
 * @author <a href="mailto:zhangtao@techwolf.cn">Kylen Zhang</a>
 * Initial created at 2014年3月13日下午4:02:13
 *
 */
public class DbwolfJdbcDataAccessProvider extends JdbcDataAccessProvider {

    protected static final Log logger = LogFactory.getLog(DbwolfJdbcDataAccessProvider.class);
        
    private SQLInterpreter interpreter;
    
    private SQLInterpreter interpreters[];
    @Override
    protected SQLInterpreter[] findSQLInterpreters() {
        return interpreters;
    }
    /**
     * @param intercepter the intercepter to set
     */
    public void setInterpreter(SQLInterpreter intercepter) {
        this.interpreter = intercepter;
        SQLInterpreter[] superInterpreters = super.findSQLInterpreters();
        SQLInterpreter[] result = new SQLInterpreter[superInterpreters.length + 1];
        System.arraycopy(superInterpreters, 0, result, 0, superInterpreters.length);
        result[superInterpreters.length]=intercepter;
        interpreters = result;
        
    }

    
}
