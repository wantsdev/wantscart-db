package com.tucaohui.dbwolf.admin;

import java.util.List;

import com.tucaohui.dbwolf.admin.config.AdminConfig;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import cn.techwolf.dbwolf.zookeeper.ZKClient;

/**
 * 未完成，暂不能使用！！！
 * 
 * 管理台命令，删除指定的数据库. 命令定义
 * 
 * name: deletedb usage: deletedb [-p,--dependency] dbname ... options :
 * -i,--ignore 删除[dbname]指定的数据配置,不理睬相关的其他数据库 -p,--dependency
 * 删除[dbname]指定的数据配置,并且循环地删除依赖数据库
 * 
 */
public final class DeleteDb extends StormCommand {

    private final ZKClient zkClient;

    private Options opt;

    private HelpFormatter helper = new HelpFormatter();

    private CommandLineParser parser = new PosixParser();

    public DeleteDb() {
        this.zkClient = createZKClient();
        this.opt = createOptions();
        this.helper = createHelper();
    }

    @Override
    public Options createOptions() {
        Options opt = new Options();
        opt.addOption("a", "all", false, "删除所有数据配置");
        opt.addOption("p", "dependency", true, "删除[dbname]指定的数据配置,并且循环地删除依赖数据库");
        opt.addOption("i", "ignore", true, "删除[dbname]指定的数据配置,不理睬相关的其他数据库");
        opt.addOption("h", "help", true, "查看帮助");
        return opt;
    }

    private HelpFormatter createHelper() {
        return new HelpFormatter();
    }

    @Override
    public void printHelp() {
        helper.printHelp("options", opt);
    }

    @Override
    public void responseCmd(String[] args) {
        CommandLine cl = null;
        try {
            cl = parser.parse(opt, args);
        } catch (ParseException e) {
            printHelp();
            return;
        }
        if (cl.hasOption("h")) {
            printHelp();
        } else if (cl.hasOption("a")) {
            responseDeleteAll(AdminConfig.getConfig().getZKRoot());
        } else if (cl.hasOption("i")) {
            responseDelete(cl.getOptionValue("i"), false);
        } else if (cl.hasOption("p")) {
            responseDelete(cl.getOptionValue("p"), true);
        } else {
            printHelp();
        }
    }

    private void responseDelete(String db, boolean deleteDp) {
        String delePath = cacuDbPath(db);
        if (deleteDp) {
            String refersPath = cacuDbPath(db) + "/references";
            if (zkClient.exist(refersPath) != null) {
                List<String> refs = zkClient.getChildren(refersPath);
                if (refs.size() > 0) {
                    for (String refDb : refs) {
                        String refPath = cacuDbPath(refDb);
                        responseDeleteAll(refPath);
                        zkClient.deleteNode(refPath);
                        System.out.println("delete reference db [" + refDb + "]");
                    }
                }
            }
        }
        responseDeleteAll(delePath);
        zkClient.deleteNode(delePath);
        System.out.println("delete db [" + db + "]");
    }

    private void responseDeleteAll(String path) {
        if (zkClient.exist(path) != null) {
            List<String> children = zkClient.getChildren(path);
            if (children.size() > 0) {
                for (String child : children) {
                    String cpath = path + "/" + child;
                    responseDeleteAll(cpath);
                    zkClient.deleteNode(cpath);
                }
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("开始执行删除任务：");
        DeleteDb cmd = new DeleteDb();
        long begin = System.currentTimeMillis();
        cmd.responseCmd(args);
        long end = System.currentTimeMillis();
        System.out.println("任务完成，用时 " + (end - begin) + " ms");
    }
}
