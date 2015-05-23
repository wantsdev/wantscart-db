package cn.techwolf.dbwolf.admin;

import org.apache.commons.cli.Options;

import cn.techwolf.dbwolf.admin.config.AdminConfig;
import cn.techwolf.dbwolf.zookeeper.ZKClient;

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
