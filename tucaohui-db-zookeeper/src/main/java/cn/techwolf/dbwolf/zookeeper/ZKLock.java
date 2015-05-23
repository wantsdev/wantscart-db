package cn.techwolf.dbwolf.zookeeper;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ZKLock extends ReentrantLock {

    private static final long serialVersionUID = 1L;

    private final Condition stateCondition = newCondition();

    private final Condition znodeCondition = newCondition();

    private final Condition dataCondition = newCondition();

    public Condition getStateCondition() {
        return stateCondition;
    }

    public Condition getZnodeCondition() {
        return znodeCondition;
    }

    public Condition getDataCondition() {
        return dataCondition;
    }

}
