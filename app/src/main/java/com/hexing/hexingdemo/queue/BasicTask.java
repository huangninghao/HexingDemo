package com.hexing.hexingdemo.queue;

/**
 * @author caibinglong
 *         date 2018/6/28.
 *         desc desc
 */


public abstract class BasicTask implements ITask {

    // 默认优先级。
    private Priority priority = Priority.DEFAULT;
    private int sequence;

    @Override
    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    @Override
    public Priority getPriority() {
        return priority;
    }

    @Override
    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    @Override
    public int getSequence() {
        return sequence;
    }

    // 做优先级比较。
    @Override
    public int compareTo(ITask another) {
        final Priority me = this.getPriority();
        final Priority it = another.getPriority();
        return me == it ? this.getSequence() - another.getSequence() :
                it.ordinal() - me.ordinal();
    }
}
