package com.tucaohui.jade.datasource.router;

import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicLong;

import com.tucaohui.jade.datasource.Router;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RoundRouter implements Router {

    // 输出日志
    protected static final Log logger = LogFactory.getLog(RoundRouter.class);

    protected AtomicLong counter = new AtomicLong();

    protected String pattern;

    protected int count;

    /**
     * 创建配置记录。
     * 
     * @param pattern - 数据表的名称模板
     * @param count - 散列表数目
     */
    public RoundRouter(String pattern, int count) {
        this.pattern = pattern;
        this.count = count;
    }

    @Override
    public String getColumn() {
        return null;
    }

    /**
     * 返回数据表的名称模板。
     * 
     * @return 数据表的名称模板
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * 设置数据表的名称模板。
     * 
     * @param pattern - 数据表的名称模板
     */
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    /**
     * 返回散列表数目。
     * 
     * @return 散列表数目
     */
    public int getCount() {
        return count;
    }

    /**
     * 设置散列表数目。
     * 
     * @param count - 散列表数目
     */
    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String doRoute(Object columnValue) {

        if (pattern != null) {

            // 计算业务名称
            int value = (int) (counter.getAndAdd(1) % count);

            String name = MessageFormat.format(pattern, value);

            // 输出日志
            if (logger.isDebugEnabled()) {
                logger.debug("Routing to: " + name);
            }

            return name;
        }

        return null;
    }
}
