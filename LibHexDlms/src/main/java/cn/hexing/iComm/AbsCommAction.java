package cn.hexing.iComm;

import cn.hexing.model.CommPara;

/**
 * @author caibinglong
 *         date 2018/9/25.
 *         desc desc
 */

public abstract class AbsCommAction implements ICommAction {
    @Override
    public boolean openDevice(CommPara para) {
        return false;
    }

    @Override
    public boolean closeDevice() {
        return false;
    }

    @Override
    public byte[] receiveBytToCallback(int waitT, int byteLen) {
        return new byte[0];
    }

    @Override
    public byte[] receiveByt(int SleepT, int WaitT) {
        return new byte[0];
    }

    /**
     * 串口数据 接收
     *
     * @param WaitT     接收数据超时时间
     * @param isNeedCon 收到数据是否处理 true 需要
     * @return byte[]
     */
    public byte[] receiveByt(int SleepT, int WaitT, boolean isNeedCon) {
        return new byte[0];
    }

    public byte[] receiveByte(int waitTime, int checkFilter, int byteLen) {
        return new byte[0];
    }

    @Override
    public boolean sendByt(byte[] sndByte) {
        return false;
    }

    @Override
    public void setBaudRate(int baudRate) {

    }

    @Override
    public void setBaudRate(int baudRate, char parity, int dataBit, int stopBit) {

    }

    public void setDBitAndParity(int dataBit, char parity) {

    }
}
