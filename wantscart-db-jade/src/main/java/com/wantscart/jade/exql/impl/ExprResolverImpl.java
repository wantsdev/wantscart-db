package com.wantscart.jade.exql.impl;

import com.wantscart.jade.exql.ExprResolver;
import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.apache.commons.jexl.Expression;
//import org.apache.commons.jexl.ExpressionFactory;
//import org.apache.commons.jexl.JexlContext;
//import org.apache.commons.jexl.JexlHelper;

/**
 * 默认使用: Apache Common Jexl引擎实现表达式处理。<br/>
 * 在构造Expression前需要将字符串中的[name]替换为['name']，使Jexl可以识别。<br/>
 * <br/>
 * 前缀为[:]的变量名称可以是字符串或者数字，最终通过参数map填充。<br/>
 * 前缀为[$]的变量名称只能是字符串，最终通过常量map填充。<br/>
 * 
 * @author han.liao
 */
public class ExprResolverImpl implements ExprResolver {

    private static final Log logger = LogFactory.getLog(ExprResolverImpl.class);

    // 表达式的缓存
    protected static final ConcurrentHashMap<String, Expression> cache = new ConcurrentHashMap<String, Expression>();

    // 正则表达式
    private static final Pattern PREFIX_PATTERN = Pattern.compile( // NL
            "(\\:|\\$)([a-zA-Z0-9_]+)(\\.[a-zA-Z0-9_]+)*");

    private static final Pattern MAP_PATTERN = Pattern.compile( // NL
            "\\[([\\.a-zA-Z0-9_]+)\\]");

    // 常量前缀
    private static final String CONST_PREFIX = "_mapConsts";

    // 参数前缀
    private static final String VAR_PREFIX = "_mapVars";

    // 参数表
    protected final Map<String, Object> mapVars = new HashMap<String, Object>();

    // 常量表
    protected final Map<String, Object> mapConsts = new HashMap<String, Object>();

    // Common Jexl 上下文
    protected final JexlContext context = new MapContext();

    private static final JexlEngine engine = new JexlEngine();

    /**
     * 构造表达式处理器。
     */
    public ExprResolverImpl() {
        context.set(VAR_PREFIX, mapVars);
        context.set(CONST_PREFIX, mapConsts);
    }

    /**
     * 构造表达式处理器。
     * 
     * @param mapVars - 初始的参数表
     */
    public ExprResolverImpl(Map<String, ?> mapVars) {
        this();
        this.mapVars.putAll(mapVars);
    }

    /**
     * 构造表达式处理器。
     * 
     * @param mapVars - 初始的参数表
     * @param mapConsts - 初始的常量表
     */
    public ExprResolverImpl(Map<String, ?> mapVars, Map<String, ?> mapConsts) {
        this();
        this.mapVars.putAll(mapVars);
        this.mapConsts.putAll(mapConsts);
    }

    /**
     * 返回表达式处理器的参数表。
     * 
     * @return 处理器的参数表
     */
    public Map<String, ?> getVars() {
        return mapVars;
    }

    /**
     * 设置表达式处理器的参数表。
     * 
     * @param map - 处理器的参数表
     */
    public void setVars(Map<String, ?> map) {
        mapVars.putAll(map);
    }

    /**
     * 返回表达式处理器的常量表。
     * 
     * @return 处理器的常量表
     */
    public Map<String, ?> getConstants() {
        return mapConsts;
    }

    /**
     * 设置表达式处理器的常量表。
     * 
     * @param map - 处理器的常量表
     */
    public void setConstants(Map<String, ?> map) {
        mapConsts.putAll(map);
    }

    @Override
    public Object executeExpr(final String expression) throws Exception {

        // 从缓存中获取解析的表达式
        Expression expr = cache.get(expression);

        if (expr == null) {
            //
            StringBuilder builder = new StringBuilder(expression.length() * 2);

            // 将[name]替换为['name']
            Matcher mapMatcher = MAP_PATTERN.matcher(expression);
            int index = 0;
            while (mapMatcher.find()) {
                builder.append(expression.substring(index, mapMatcher.start()));
                String t = mapMatcher.group(1);
                if (!NumberUtils.isDigits(t)) {
                    builder.append("['");
                    builder.append(mapMatcher.group(1));
                    builder.append("']");
                } else {
                    builder.append(mapMatcher.group(0));
                }
                index = mapMatcher.end();
            }

            String expression2;
            if (builder.length() == 0) {
                expression2 = expression;
            } else {
                builder.append(expression.substring(index));
                expression2 = builder.toString();
                builder.setLength(0);
            }

            index = 0;

            // 匹配正则表达式, 并替换内容
            Matcher matcher = PREFIX_PATTERN.matcher(expression2);
            while (matcher.find()) {

                builder.append(expression2.substring(index, matcher.start()));

                String prefix = matcher.group(1);
                String name = matcher.group(2);
                if (":".equals(prefix)) {
                    boolean isDigits = NumberUtils.isDigits(name);
                    if (isDigits) {
                        // 按顺序访问变量
                        name = ':' + name;
                    }

                    if (!mapVars.containsKey(name)) {
                        throw new IllegalArgumentException("Variable \'" + name
                                + "\' not defined in DAO method");
                    }

                    // 按名称访问变量
                    builder.append(VAR_PREFIX);
                    builder.append("['");
                    builder.append(name);
                    builder.append("']");

                } else if ("$".equals(prefix)) {

                    if (!mapConsts.containsKey(name)) {
                        throw new IllegalArgumentException("Constant \'" + name
                                + "\' not defined in DAO class");
                    }

                    // 拼出常量访问语句
                    builder.append(CONST_PREFIX);
                    builder.append("[\'");
                    builder.append(name);
                    builder.append("\']");
                }

                index = matcher.end(2);
            }

            builder.append(expression2.substring(index));

            if (logger.isDebugEnabled()) {
                logger.debug("Create Jexl Expression " + builder.toString());
            }

            // 编译表达式
            expr = engine.createExpression(builder.toString());
            cache.putIfAbsent(expression2, expr);
        }

        // 进行表达式求值
        return expr.evaluate(context);
    }

    @Override
    public Object getVar(String variant) {
        return mapVars.get(variant);
    }

    @Override
    public void setVar(String variant, Object value) {
        mapVars.put(variant, value);
    }

}
