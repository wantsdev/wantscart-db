package com.wantscart.jade.exql.impl;

import com.wantscart.jade.core.SerializableColumnHandler;
import com.wantscart.jade.core.Serializer;
import com.wantscart.jade.exql.ExprResolver;
import com.wantscart.jade.exql.ExqlContext;
import com.wantscart.jade.exql.ExqlUnit;
import com.wantscart.jade.exql.util.ExqlUtils;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Proxy;

/**
 * 输出表达式内容的语句单元, 例如: ':expr' 或者: '#(:expr)' 形式的表达式。
 *
 * @author han.liao
 */
public class ExprUnit implements ExqlUnit {

    private final String expr;

    /**
     * 构造输出表达式内容的语句单元。
     *
     * @param text - 输出的表达式
     */
    public ExprUnit(String expr) {
        if (StringUtils.contains(expr, ".")) {
            String[] vars = StringUtils.split(expr, ".");
            if (vars != null && vars.length > 1) {
                StringBuilder newExpr = new StringBuilder(vars[0]);
                for (int i = 1; i < vars.length; i++) {
                    newExpr.append("[").append(vars[i]).append("]");
                }
                expr = newExpr.toString();
            }
        }
        this.expr = expr;
    }

    @Override
    public boolean isValid(ExprResolver exprResolver) {

        // 解释表达式内容
        Object obj = ExqlUtils.execExpr(exprResolver, expr);

        // 表达式内容有效
        return ExqlUtils.isValid(obj);
    }

    @Override
    public void fill(ExqlContext exqlContext, ExprResolver exprResolver) throws Exception {

        // 解释表达式内容
        Object obj = exprResolver.executeExpr(expr);

//        if (obj instanceof Proxy) {
//            if (obj instanceof Serializer) {
//                obj = ((Serializer) obj).serialize(obj);
//            }
//        }

        // 输出转义的对象内容
        exqlContext.fillValue(obj);
    }

    @Override
    public void toXml(StringBuffer xml, String prefix) {
        xml.append(prefix).append("<unit>\n");
        xml.append(prefix).append(BLANK).append("<type>ExprUnit</type>\n");
        xml.append(prefix).append(BLANK).append("<expr>" + expr + "</expr>\n");
        xml.append(prefix).append("</unit>\n");

    }
}
