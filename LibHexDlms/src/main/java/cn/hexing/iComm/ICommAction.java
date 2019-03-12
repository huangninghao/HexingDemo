package cn.hexing.iComm;


import cn.hexing.model.CommPara;

public interface ICommAction {

    /***
     * 打开通讯模块
     * @param para 参数配置对象
     * @return bool
     */
    boolean openDevice(CommPara para);

    /***
     * 关闭串口
     * @return bool
     */
    boolean closeDevice();

    /**
     * 数据接收 一般回调处理
     *
     * @param waitT   超时时间
     * @param byteLen 读取字节len
     */
    byte[] receiveBytToCallback(int waitT, int byteLen);

    /***
     * 接受
     * @param SleepT 等待时间
     * @param WaitT 超时时间
     * @return byte[]
     */
    byte[] receiveByt(int SleepT, int WaitT, int byteLen);

    /***
     * 接受
     * @param SleepT 等待时间
     * @param WaitT 超时时间
     * @return byte[]
     */
    byte[] receiveByt(int SleepT, int WaitT);

    /***
     * 发送
     * @param sndByte byte[]
     * @return bool
     */
    boolean sendByt(byte[] sndByte);

    /***
     * 设置波特率
     * @param baudRate int
     */
    void setBaudRate(int baudRate);

    void setBaudRate(int baudRate, char parity, int dataBit, int stopBit);
}
