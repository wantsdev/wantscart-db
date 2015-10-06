package com.wantscart.jade.exql.impl;

import java.util.List;

import com.wantscart.jade.exql.ExprResolver;
import com.wantscart.jade.exql.ExqlContext;
import com.wantscart.jade.exql.ExqlUnit;

/**
 * 顺序输出子单元的语句单元, 例如一个语句段。
 * 
 * @author han.liao
 */
public class BunchUnit implements ExqlUnit {

    private final List<ExqlUnit> units;

    /**
     * 构造顺序输出子单元的语句单元。
     * 
     * @param units - 子单元列表
     */
    public BunchUnit(List<ExqlUnit> units) {
        this.units = units;
    }

    @Override
    public boolean isValid(ExprResolver exprResolver) {

        // 顺序检查子单元
        for (ExqlUnit unit : units) {

            if (!unit.isValid(exprResolver)) {
                return false;
            }
        }

        // 子单元全部有效
        return true;
    }

    @Override
    public void fill(ExqlContext exqlContext, ExprResolver exprResolver) throws Exception {

        // 顺序输出子单元
        for (ExqlUnit unit : units) {
            unit.fill(exqlContext, exprResolver);
        }
    }

    @Override
    public void toXml(StringBuffer xml, String prefix) {
        xml.append(prefix).append("<unit>\n");
        xml.append(prefix).append(BLANK).append("<type>BunchUnit</type>\n");
        if (units != null && units.size() > 0) {
            for (ExqlUnit unit : units) {
                unit.toXml(xml, prefix + BLANK);
            }
        }
        xml.append(prefix).append("</unit>\n");
    }

}
