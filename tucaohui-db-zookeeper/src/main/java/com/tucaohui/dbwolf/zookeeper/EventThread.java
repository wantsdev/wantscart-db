package com.tucaohui.dbwolf.zookeeper;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 事件分发线程，用于将zookeeper修改触发的各种事件分发给listener。
 * 
 */
public class EventThread extends Thread {

    public static final Log log = LogFactory.getLog(EventThread.class);

    private static AtomicInteger eventId = new AtomicInteger(0);

    private LinkedBlockingQueue<ZKEvent> queue = new LinkedBlockingQueue<ZKEvent>();

    public EventThread(String name) {
        setDaemon(true);//不影响进程
        setName("ZKClient-EventThread-" + getId() + "-" + name);
    }

    public void run() {
        log.info("start zk-event thread");
        try {
            ZKEvent event = null;
            while (!isInterrupted()) {
                event = queue.take();//可能发生中断
                int id = eventId.incrementAndGet();
                try {
                    event.run();
                    log.debug("dispatch event " + event + ", id " + id);
                } catch (InterruptedException e) {
                    interrupt();//任务中有中断需求
                } catch (Exception e) {
                    //可能会发生异常
                    log.error("error  handling event " + event + " ,id " + id, e);
                }
            }
        } catch (InterruptedException e) {
            log.info("zk-event thread interrupted");
        }
    }

    public void send(final ZKEvent zkEvent) {
        queue.add(zkEvent);
    }
}
