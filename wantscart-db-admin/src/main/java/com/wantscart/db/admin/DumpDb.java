package com.wantscart.db.admin;

import com.wantscart.db.admin.config.AdminConfig;
import com.wantscart.db.zookeeper.ZKClient;
import org.apache.commons.cli.*;


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
