package com.wantscart.db.factory;

import com.wantscart.jade.core.JadeDaoFactoryBean;
import com.wantscart.jade.core.JadeDataAccessProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * 
 * @author <a href="mailto:zhangtao@techwolf.cn">Kylen Zhang</a>
 * Initial created at 2014年3月11日下午2:20:34
 *
 * @param <T>
 */
public class DbwolfJadeDaoBeanFactory<T> extends JadeDaoFactoryBean<T> implements ApplicationContextAware{
    
    protected static final Log logger = LogFactory.getLog(DbwolfJadeDaoBeanFactory.class);

    private ApplicationContext applicationContext;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.dataAccessProvider == null) {
            JadeDataAccessProvider provider = applicationContext.getBean("jade.dataAccessProvider",JadeDataAccessProvider.class);
            this.setDataAccessProvider(provider);
        }
        super.afterPropertiesSet();
    }

    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    
}
