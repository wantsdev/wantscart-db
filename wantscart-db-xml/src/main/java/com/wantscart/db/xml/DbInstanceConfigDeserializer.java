package com.wantscart.db.xml;

import com.wantscart.db.zookeeper.DataDeserializer;
import com.wantscart.db.zookeeper.exception.ZKDataDeserializeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * 将配置信息反序列化为DbInstanceConfig实例.
 * 
 */
public class DbInstanceConfigDeserializer implements DataDeserializer<DbInstanceConfig> {

    public static final Log log = LogFactory.getLog(DbInstanceConfigDeserializer.class);

    public DbInstanceConfig deserialize(final byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        try {
            String xml = new String(data, "utf-8");//xml配置必须为utf-8字符集编写
            log.debug("xml config: " + xml);
            DbXmlParser xmlConfig = new DbXmlParser(xml);
            return xmlConfig.getDbInstanceConfig();
        } catch (Exception e) {
            throw new ZKDataDeserializeException(e);
        }
    }
}
