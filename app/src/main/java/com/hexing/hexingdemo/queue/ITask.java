package com.hexing.hexingdemo.queue;

/**
 * @author caibinglong
 *         date 2018/6/28.
 *         desc desc
 */

public interface ITask extends Comparable<ITask> {
    void run();

    void setPriority(Priority priority);

    Priority getPriority();

    void setSequence(int sequence);

    int getSequence();
}
