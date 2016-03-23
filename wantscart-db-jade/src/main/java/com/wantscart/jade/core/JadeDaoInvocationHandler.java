/*
 * Copyright 2009-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License i distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wantscart.jade.core;

import com.wantscart.jade.annotation.DAO;
import com.wantscart.jade.annotation.SQL;
import com.wantscart.jade.annotation.SQLParam;
import com.wantscart.jade.core.serializer.NullSerializer;
import com.wantscart.jade.provider.DataAccess;
import com.wantscart.jade.provider.Definition;
import com.wantscart.jade.provider.Modifier;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public class JadeDaoInvocationHandler implements InvocationHandler {

    private static final Log logger = LogFactory.getLog(JadeDaoInvocationHandler.class);

    private static JadeOperationFactory jdbcOperationFactory = new JadeOperationFactoryImpl();

    private HashMap<Method, JadeOperation> jdbcOperations = new HashMap<Method, JadeOperation>();

    private final ApplicationContext applicationContext;

    private final Definition definition;

    private final DataAccess dataAccess;

    public JadeDaoInvocationHandler(ApplicationContext applicationContext, DataAccess dataAccess, Definition definition) {
        this.applicationContext = applicationContext;
        this.definition = definition;
        this.dataAccess = dataAccess;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg == null) {
                    continue;
                }
                Class<?> argType = arg.getClass();
                if (!TypeUtils.isBaseType(argType) && !(arg instanceof Collection)) {
                    TableSchema schema = TableSchema.getSchema(argType);
                    if(schema != null){
                        Map mapArg = new HashMap();
                        for(TableSchema.Column column : schema.getColumns()){
                            Object field = column.getGetter().invoke(arg);
                            if(column.isSerializable()){
                                if(column.getSerializer() instanceof NullSerializer && field instanceof Serializable){
                                    field = ((Serializable) field).serialize();
                                } else {
                                    field = column.getSerializer().serialize(field);
                                }
                            }
                            mapArg.put(column.getOriginName(), field);
                        }
                        args[i] = mapArg;
                    }
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger
                    .debug("invoking  " + definition.getDAOClazz().getName() + "#"
                            + method.getName());
        }

        if (Object.class == method.getDeclaringClass()) {
            String methodName = method.getName();
            if (methodName.equals("toString")) {
                return JadeDaoInvocationHandler.this.toString();
            }
            if (methodName.equals("hashCode")) {
                return definition.getDAOClazz().hashCode() * 13 + this.hashCode();
            }
            if (methodName.equals("equals")) {
                return args[0] == proxy;
            }
            if (methodName.equals("clone")) {
                throw new CloneNotSupportedException("clone is not supported for jade dao.");
            }
            throw new UnsupportedOperationException(definition.getDAOClazz().getName() + "#"
                    + method.getName());
        }

        JadeOperation operation = jdbcOperations.get(method);
        if (operation == null) {
            synchronized (jdbcOperations) {
                operation = jdbcOperations.get(method);
                if (operation == null) {
                    Modifier modifier = new Modifier(definition, method);
                    operation = jdbcOperationFactory.getOperation(dataAccess, modifier);
                    jdbcOperations.put(method, operation);
                }
            }
        }
        //
        // 将参数放入  Map
        Map<String, Object> parameters;
        if (args == null || args.length == 0) {
            parameters = new HashMap<String, Object>(4);
        } else {
            parameters = new HashMap<String, Object>(args.length * 2 + 4);
            SQLParam[] sqlParams = operation.getModifier().getParameterAnnotations(SQLParam.class);
            for (int i = 0; i < args.length; i++) {
                parameters.put(":" + (i + 1), args[i]);
                SQLParam sqlParam = sqlParams[i];
                if (sqlParam != null) {
                    parameters.put(sqlParam.value(), args[i]);
                }
            }
        }
        //
        if (logger.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("invoking ").append(definition.getDAOClazz().getName()).append("#").append(
                    method.getName()).append("\n");
            sb.append("\toperation: ").append(operation.getClass().getSimpleName()).append("\n");
            sb.append("\tsql: ").append(operation.getModifier().getAnnotation(SQL.class).value())
                    .append("\n");
            sb.append("\tparams: ");
            ArrayList<String> keys = new ArrayList<String>(parameters.keySet());
            Collections.sort(keys);
            for (String key : keys) {
                sb.append(key).append("='").append(parameters.get(key)).append("'  ");
            }
            logger.debug(sb.toString());
        }

        return operation.execute(parameters);
    }

    @Override
    public String toString() {
        DAO dao = definition.getDAOClazz().getAnnotation(DAO.class);
        String toString = definition.getDAOClazz().getName()//
                + "[catalog=" + dao.catalog() + "]";
        return toString;
    }

}
