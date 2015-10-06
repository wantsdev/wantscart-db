package com.wantscart.db.admin;

import com.wantscart.db.admin.config.AdminConfig;
import com.wantscart.db.zookeeper.ZKClient;
import org.apache.commons.cli.Options;


/**
 * dbwolf控制台命令.
 * 
 */
public abstract class StormCommand {

    /**
     * 根据db名称构造zookeeper path.
     * 
     * @param db
     * @return
     */
    protected static String cacuDbPath(final String db) {
        return AdminConfig.getConfig().getZKRoot() + "/" + db;
    }

    protected ZKClient createZKClient() {
        AdminConfig config = AdminConfig.getConfig();
        return new ZKClient(config.getEndpoints());
    }

    protected abstract Options createOptions();

    protected abstract void printHelp();

    protected abstract void responseCmd(String[] args);

    public static interface StormTask extends Runnable {

    }
}
