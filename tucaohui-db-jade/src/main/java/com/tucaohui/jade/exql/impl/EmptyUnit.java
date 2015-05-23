package com.tucaohui.jade.exql.impl;

import com.tucaohui.jade.exql.ExprResolver;
import com.tucaohui.jade.exql.ExqlContext;
import com.tucaohui.jade.exql.ExqlUnit;

/**
 * 输出空白的语句单元, 代替空的表达式。
 * 
 * @author han.liao
 */
public class EmptyUnit implements ExqlUnit {

    @Override
    public boolean isValid(ExprResolver exprResolver) {
        // Empty unit is always valid.
        return true;
    }

    @Override
    public void fill(ExqlContext exqlContext, ExprResolver exprResolver) throws Exception {
        // Do nothing.
    }

    @Override
    public void toXml(StringBuffer xml, String prefix) {
        xml.append(prefix).append("<unit>\n");
        xml.append(prefix).append("<type>EmptyUnit</type>\n");
        xml.append(prefix).append("</unit>\n");

    }
}
