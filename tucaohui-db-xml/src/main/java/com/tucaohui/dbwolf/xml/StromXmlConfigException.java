package com.tucaohui.dbwolf.xml;

/**
 * 数据服务配置信息异常.
 * 
 */
public class StromXmlConfigException extends Exception {

    private static final long serialVersionUID = 1L;

    private String xml = "";

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public StromXmlConfigException(final String msg) {
        super(msg);
    }

    public StromXmlConfigException(final Throwable cause) {
        initCause(cause);
    }

    public StromXmlConfigException(final String msg, final Throwable cause) {
        super(msg);
        initCause(cause);
    }
}
