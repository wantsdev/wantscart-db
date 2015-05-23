package cn.techwolf.dbwolf.xml;

/**
 * xml配置文件格式或字符异常,此异常可由<code>StromXmlConfigException</code>引发.
 * 
 */
public class InstanceXmlConfigException extends Exception {

    private static final long serialVersionUID = 1L;

    public InstanceXmlConfigException(final String xml) {
        super("xml config error [" + xml + "]");
    }

    public InstanceXmlConfigException(final String xml, Throwable cause) {
        super("xml config error [" + xml + "]");
        initCause(cause);
    }
}
