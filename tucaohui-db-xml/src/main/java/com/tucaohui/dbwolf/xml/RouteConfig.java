package com.tucaohui.dbwolf.xml;

/**
 * 路由配置信息.
 * 
 */
public class RouteConfig {

    /**
     * 路由匹配规则,按正则表达式形式.
     */
    private String expression;

    /**
     * 对应的数据库服务器名称.
     */
    private String instance;

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        sb.append("expression:");
        sb.append(expression);
        sb.append(",");
        sb.append("instance:");
        sb.append(instance);
        sb.append("}");
        return sb.toString();
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(final String expression) {
        this.expression = expression;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(final String instance) {
        this.instance = instance;
    }

}
