package cn.hexing.model;

public class CommPara {

    // COM口名称
    public String ComName;

    // 波特率
    public int BRate;

    // 数据位数
    public int DBit;

    // 校验位
    public char Pty;

    // 停止位
    public int Sbit;

    // 是否控制电源
    public Boolean IsControlPower;

    public enum StopBits {
        None,
        // 摘要:
        // 使用一个停止位。
        One1,
        //
        // 摘要:
        // 使用两个停止位。
        Two,
        //
        // 摘要:
        // 使用 1.5 个停止位。
        OnePointFive
    }

    public enum Parity {
        // 摘要:
        // 不发生奇偶校验检查。
        None,
        //
        // 摘要:
        // 设置奇偶校验位，使位数等于奇数。
        Odd,
        //
        // 摘要:
        // 设置奇偶校验位，使位数等于偶数。
        Even,
        //
        // 摘要:
        // 将奇偶校验位保留为 1。
        Mark,
        //
        // 摘要:
        // 将奇偶校验位保留为 0。
        Space
    }

}
