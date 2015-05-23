package com.tucaohui.dbwolf.admin;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tucaohui.dbwolf.admin.config.AdminConfig;
import cn.techwolf.dbwolf.zookeeper.DataSerializer;
import cn.techwolf.dbwolf.zookeeper.ZKClient;
import cn.techwolf.dbwolf.zookeeper.exception.ZKDataSerializeException;

/**
 * 管理台命令，用于修改配置文件后重新加载.
 * 
 * 命令定义. name: updateJadeConfig usage: updateJadeConfig -f,--file
 * [pathname] options : -f,--file 从[pathname]中指定的配置文件中读取，并刷新Jade Config配置
 * 
 * 
 */
public final class UpdateJadeConfig extends StormCommand {

    public static final Log log = LogFactory.getLog(UpdateJadeConfig.class);

    private final ZKClient zkClient;

    private final Options opt;

    private final HelpFormatter helper;

    private final CommandLineParser parser;

    private final static String JADE_CONFIG_PATH = AdminConfig.getConfig().getZKRoot() + "/jade-config";

    public UpdateJadeConfig() {
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
            } else if (cl.hasOption("f")) {
                response(cl.getOptionValue("f"));
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

    private void response(String file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                byte[] b = new byte[1024];
                int n;
                while ((n = fis.read(b)) != -1) {
                    out.write(b, 0, n);
                }// end while
            } catch (Exception e) {
                throw new Exception("System error,SendTimingMms.getBytesFromFile", e);
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (Exception e) {
                        log.error("关闭文件输入流出错", e);// TODO
                    }// end try
                }// end if
            }// end try

            byte[] fileContent = out.toByteArray();
            zkClient.writeData(JADE_CONFIG_PATH, fileContent, new DataSerializer<byte[]>() {

                public byte[] serialize(byte[] obj) throws ZKDataSerializeException {
                    return obj;
                }
            }, -1);
        } catch (Exception e) {
            log.error("导入文件出错:", e);
        }

    }

    @Override
    protected Options createOptions() {
        Options opt = new Options();
        opt.addOption("f", "file", true, "从制定文件中更新Jade Config");
        opt.addOption("h", "help", true, "查看帮助");
        return opt;
    }

    public static void main(String[] args) {
        System.out.println("开始执行更新任务：");
        UpdateJadeConfig cmd = new UpdateJadeConfig();
        long begin = System.currentTimeMillis();
        cmd.responseCmd(args);
        long end = System.currentTimeMillis();
        System.out.println("任务完成，用时 " + (end - begin) + " ms");
    }

}
