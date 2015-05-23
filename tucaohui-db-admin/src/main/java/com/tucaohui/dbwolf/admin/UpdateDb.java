package com.tucaohui.dbwolf.admin;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.tucaohui.dbwolf.admin.config.AdminConfig;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NoNodeException;

import cn.techwolf.dbwolf.xml.DbInstanceConfig;
import cn.techwolf.dbwolf.xml.DbInstanceConfigDeserializer;
import cn.techwolf.dbwolf.xml.DbXmlParser.DbInstanceType;
import cn.techwolf.dbwolf.xml.InstancesXmlParser;
import cn.techwolf.dbwolf.xml.RouteConfig;
import cn.techwolf.dbwolf.xml.StromXmlConfigException;
import cn.techwolf.dbwolf.zookeeper.DataSerializer;
import cn.techwolf.dbwolf.zookeeper.ZKClient;
import cn.techwolf.dbwolf.zookeeper.exception.ZKDataSerializeException;
import cn.techwolf.dbwolf.zookeeper.exception.ZKKeeperException;

/**
 * 管理台命令，用于修改配置文件后重新加载.
 * 
 * 命令定义. name: refreshdb usage: refreshdb -r,--refresh|-a,--add [pathname]
 * options : -r,--refresh 从[pathname]中指定的配置文件中读取，并刷新已经定义过的数据库配置 -a,--add
 * 从[pathname]中指定的配置文件中读取，并添加未定义过的配置
 * 
 * 
 */
public final class UpdateDb extends StormCommand {

    public static final Log log = LogFactory.getLog(UpdateDb.class);

    private final ZKClient zkClient;

    private final Options opt;

    private final HelpFormatter helper;

    private final CommandLineParser parser;

    public UpdateDb() {
        this.zkClient = createZKClient();
        this.opt = createOptions();
        this.helper = new HelpFormatter();
        this.parser = new PosixParser();
    }

    @Override
    public void printHelp() {
        helper.printHelp("options", opt);
    }

    @Override
    public void responseCmd(String[] args) {
        try {
            CommandLine cl = parser.parse(opt, args);
            if (cl.hasOption("h")) {
                printHelp();
            } else if (cl.hasOption("a")) {
                response(cl.getOptionValue("a"), true, false);
            } else if (cl.hasOption("r")) {
                response(cl.getOptionValue("r"), false, true);
            } else {
                printHelp();
            }
        } catch (ParseException e) {
            printHelp();
            return;
        } catch (Exception e) {
            printHelp();
            log.error("发生错误", e);
            return;
        }
    }

    private void response(String file, boolean doAdd, boolean doRefresh) {
        try {
            List<UpdateTask> taskList = prepareUpdateTasks(file, doAdd, doRefresh);
            for (UpdateTask task : taskList) {
                task.run();
            }
        } catch (StromXmlConfigException e) {
            log.error("配置文件格式错误:" + e.getXml(), e);
        }

    }

    private List<UpdateTask> prepareUpdateTasks(String file, boolean doAdd, boolean doRefresh)
            throws StromXmlConfigException {
        InstancesXmlParser parser = new InstancesXmlParser(file);
        List<DbInstanceConfig> configs = parser.getDbInstances();

        List<UpdateTask> taskList = new ArrayList<UpdateTask>();

        for (DbInstanceConfig config : configs) {
            taskList.add(new UpdateTask(config, doAdd, doRefresh));
        }//检查配置文件语法，准备更新
        Collections.sort(taskList);//排序，保证signler型服务先被修改
        return taskList;
    }

    @Override
    protected Options createOptions() {
        Options opt = new Options();
        opt.addOption("r", "refresh", true, "只刷新所有数据库配置");
        opt.addOption("a", "add", true, "添加新数据库定义");
        opt.addOption("h", "help", true, "查看帮助");
        return opt;
    }

    public static void main(String[] args) {
        System.out.println("开始执行更新任务：");
        UpdateDb cmd = new UpdateDb();
        long begin = System.currentTimeMillis();
        cmd.responseCmd(args);
        long end = System.currentTimeMillis();
        System.out.println("任务完成，用时 " + (end - begin) + " ms");
    }

    /**
     * 
     * 更新任务，可排序. <br>
     * signler型数据服务的排序更靠前，以保证signler型先完成更新.
     * 
     * @author huaiyu.du@opi-corp.com 2012-2-9 下午4:08:50
     */
    private class UpdateTask implements StormTask, Comparable<UpdateTask> {

        private final DbInstanceConfig config;

        private final boolean doAdd;

        private final boolean doRefresh;

        public UpdateTask(DbInstanceConfig config, boolean doAdd, boolean doRefresh) {
            this.config = config;
            this.doAdd = doAdd;
            this.doRefresh = doRefresh;
        }

        /**
         * 比较新旧配置版本时间戳，如果更新则更新配置.
         * 
         * @param oldConfig 旧版本配置
         * @param newConfig 新版本配置
         */
        private void compareAndSet(DbInstanceConfig oldConfig, DbInstanceConfig newConfig) {
            String path = cacuDbPath(newConfig.getName());
            if (oldConfig.getTimestamp() < newConfig.getTimestamp()) {//比较版本，随后设置
                zkClient.writeData(path, newConfig.getXml(), new DataSerializer<String>() {

                    public byte[] serialize(String obj) throws ZKDataSerializeException {
                        try {
                            return obj.getBytes("utf-8");
                        } catch (UnsupportedEncodingException e) {
                            throw new ZKDataSerializeException(e);
                        }
                    }
                }, -1);
                System.out.println("update db [" + newConfig.getName() + "]");
            }
        }

        /**
         * 创建数据服务配置节点.
         * 
         * @param newConfig
         */
        private void createDb(DbInstanceConfig newConfig) {
            String path = cacuDbPath(newConfig.getName());
            boolean checkPassed = true;
            List<String> dbList = zkClient.getChildren(AdminConfig.getConfig().getZKRoot());
            //预检查,防止产生不完整结果
            if (isRouter(newConfig)) {
                for (RouteConfig route : newConfig.getRoutes()) {
                    String signlerName = route.getInstance();
                    if (!dbList.contains(signlerName)) {
                        checkPassed = false;
                        System.out.println("=================================================");
                        System.out.println("创建router[" + newConfig.getName() + "]失败！");
                        System.out.println("原因:signler数据服务[" + signlerName + "]未定义，请检查配置文件！");
                        System.out.println("配置信息：[" + newConfig.getXml() + "]");
                    }
                }

                if (!checkPassed) {
                    return;
                }
            }

            //signler型或者经过验证的router型，将会创建配置项
            if (!isRouter(newConfig) || checkPassed) {
                //创建配置节点
                zkClient.createPersistent(path, newConfig.getXml(), new DataSerializer<String>() {

                    public byte[] serialize(String obj) throws ZKDataSerializeException {
                        try {
                            return obj.getBytes("utf-8");
                        } catch (UnsupportedEncodingException e) {
                            throw new ZKDataSerializeException(e);
                        }
                    }
                }, true);
            }

            //为router型创建依赖和引用关系
            if (isRouter(newConfig) && checkPassed) {
                final String routerName = newConfig.getName();
                for (RouteConfig route : newConfig.getRoutes()) {
                    String signlerName = route.getInstance();
                    createDependency(routerName, signlerName);
                    createReference(signlerName, routerName);
                }
            }

            System.out.println("add db [" + newConfig.getName() + "]");
        }

        /**
         * 创建依赖节点.
         * 
         * @param routerName 路由名称
         * @param signlerName 单例名称
         */
        private void createDependency(final String routerName, final String signlerName) {
            String dpath = cacuDbPath(routerName) + "/dependencies/" + signlerName;
            try {
                zkClient.createPersistent(dpath, null, null, true);
            } catch (ZKKeeperException e) {
                if (e.getCause() instanceof KeeperException.NodeExistsException) {} else throw e;
            }
        }

        /**
         * 创建引用节点.
         * 
         * @param signlerName 单例名称
         * @param routerName 路由名称
         */
        private void createReference(final String signlerName, final String routerName) {
            String refPath = cacuDbPath(signlerName) + "/references/" + routerName;
            try {
                zkClient.createPersistent(refPath, null, null, true);
            } catch (ZKKeeperException e) {
                if (e.getCause() instanceof KeeperException.NodeExistsException) {} else throw e;
            }

        }

        private boolean isRouter(DbInstanceConfig newConfig) {
            return newConfig.getType() == DbInstanceType.CDbInstanceRouter;
        }

        /**
         * 获取旧版本数据服务配置.
         * 
         * @param config
         * @return
         */
        private DbInstanceConfig getOldVersion(DbInstanceConfig config) {
            String path = cacuDbPath(config.getName());
            return zkClient.readData(path, new DbInstanceConfigDeserializer());
        }

        /**
         * 执行更新任务.
         */
        public void run() {
            boolean noNode = false;
            try {
                DbInstanceConfig oldVersion = getOldVersion(config);
                if (doRefresh) {
                    compareAndSet(oldVersion, config);
                }
            } catch (Exception e) {
                if (e.getCause() instanceof NoNodeException) {
                    noNode = true;
                }
            }
            if (doAdd && noNode) {
                createDb(config);
            }
        }

        public int compareTo(UpdateTask o) {
            int p1 = cacuPriority(this.config.getType());
            int p2 = cacuPriority(o.config.getType());
            if (p1 < p2) {
                return -1;
            } else if (p1 == p2) {
                return 0;
            } else {
                return 1;
            }
        }

        public int cacuPriority(DbInstanceType type) {
            if (type == DbInstanceType.CDbInstanceSingler) {
                return 1;
            }
            return 2;
        }
    }
}
