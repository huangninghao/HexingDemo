package cn.hexing;

/**
 * @author caibinglong
 *         date 2018/3/9.
 *         desc 操作类型 读取 写 读数据块
 */

public class HexAction {
    public final static int ACTION_READ = 0x01;
    public final static int ACTION_WRITE = 0x02;
    public final static int ACTION_EXECUTE = 0x03;
    public final static int ACTION_READ_BLOCK = 0x04;
    //读取多个obis
    public final static int ACTION_READ_MULTIPLE = 0x05;
    public final static int ACTION_EXECUTE_READ = 0x06;

}
