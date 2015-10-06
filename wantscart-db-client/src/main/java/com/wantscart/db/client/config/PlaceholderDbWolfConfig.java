/**
 * techwolf.cn All rights reserved.
 */
package com.wantscart.db.client.config;

import org.apache.commons.lang.StringUtils;

/**
 * Comments for PlaceholderDbWolfConfig.java
 * 
 * @author <a href="mailto:liujun@techwolf.cn">刘军</a>
 * @createTime 2014年3月28日 上午11:30:27
 */
public class PlaceholderDbWolfConfig extends DbWolfConfig {

    private static final DbWolfConfig defaultConfig = new EnvDbWolfConfig();

    private String endpoints;

    private String dsFactoryClz;

    private String zkRoot;

    /* (non-Javadoc)
     * @see DbWolfConfig#getEndpoints()
     */
    @Override
    public String getEndpoints() {
        return StringUtils.defaultIfEmpty(endpoints, defaultConfig.getEndpoints());
    }

    /* (non-Javadoc)
     * @see DbWolfConfig#getDsFactoryClz()
     */
    @Override
    public String getDsFactoryClz() {
        return StringUtils.defaultIfEmpty(dsFactoryClz, defaultConfig.getDsFactoryClz());
    }

    /* (non-Javadoc)
     * @see DbWolfConfig#getZkRoot()
     */
    @Override
    public String getZkRoot() {
        return StringUtils.defaultIfEmpty(zkRoot, defaultConfig.getZkRoot());
    }

    /**
     * @param endpoints the endpoints to set
     */
    public void setEndpoints(String endpoints) {
        this.endpoints = endpoints;
    }

    /**
     * @param dsFactoryClz the dsFactoryClz to set
     */
    public void setDsFactoryClz(String dsFactoryClz) {
        this.dsFactoryClz = dsFactoryClz;
    }

    /**
     * @param zkRoot the zkRoot to set
     */
    public void setZkRoot(String zkRoot) {
        this.zkRoot = zkRoot;
    }

}
