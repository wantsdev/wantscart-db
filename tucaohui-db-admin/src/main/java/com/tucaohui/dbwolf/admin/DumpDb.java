package com.tucaohui.dbwolf.admin;

import com.tucaohui.dbwolf.admin.config.AdminConfig;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import cn.techwolf.dbwolf.zookeeper.ZKClient;

public class DumpDb extends StormCommand {

    private final ZKClient zkClient;

    private Options opt;

    private HelpFormatter helper = new HelpFormatter();

    private CommandLineParser parser = new PosixParser();

    public DumpDb() {
        this.zkClient = createZKClient();
        this.opt = createOptions();
        this.helper = createHelper();
    }

    private HelpFormatter createHelper() {
        return new HelpFormatter();
    }

    @Override
    protected Options createOptions() {
        Options opt = new Options();
        opt.addOption("a", "all", false, "导出所有数据配置");
        return opt;
    }

    @Override
    protected void printHelp() {
        helper.printHelp("options", opt);
    }

    @Override
    protected void responseCmd(String[] args) {
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
            responseDump(AdminConfig.getConfig().getZKRoot());
        }
    }

    private void responseDump(String path) {
    }

}
