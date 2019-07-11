package cn.hexing.dlt645.comm;


import android.os.SystemClock;

import com.android.SerialPort.SerialPort;

import org.greenrobot.eventbus.EventBus;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.hexing.EventMsg;
import cn.hexing.HexDevice;
import cn.hexing.HexStringUtil;
import cn.hexing.dlt645.CommServer;
import cn.hexing.dlt645.HexClient645API;
import cn.hexing.dlt645.SendFrameTypes;
import cn.hexing.dlt645.iprotocol.IFrame;
import cn.hexing.dlt645.model.ReceiveModel;
import cn.hexing.iComm.AbsCommAction;
import cn.hexing.model.CommPara;

/**
 * 串口通讯
 */
public class CommOpticalSerialPort extends AbsCommAction {
    private final static String TAG = CommOpticalSerialPort.class.getSimpleName();
    private FileDescriptor mfd;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private SerialPort mSerialPort = new SerialPort();
    private int stopBit;
    private char parity;
    private int dataBit;
    private int baudRate;
    private String uartpath;
    // 第一字节接收超时时间
    // 字节与字节之间超时时间
    private static boolean delay_occurZJ = false;
    private Timer timer = new Timer();
    private TimerTask timerTask;
    private static boolean aOpenStatus = false;

    protected final static byte[] BroadCastZigbeeAddress = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
    protected final static byte[] BroadCast645Address = new byte[]{(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA};
    protected final static byte[] BroadCastZigbeeShortAddress = new byte[]{(byte) 0xFF, (byte) 0xFE};

    public void setSsSendFrameType(@SendFrameTypes.SFrameTypes int ssSendFrameType) {
        this.ssSendFrameType = ssSendFrameType;
    }

    protected @SendFrameTypes.SFrameTypes
    int ssSendFrameType;
    protected IFrame ssSendFrame;

    @Override
    public boolean openDevice(CommPara cpara) {
        // 停止位
        stopBit = cpara.Sbit;
        // 校验位
        parity = cpara.Pty;
        // 数据位
        dataBit = cpara.DBit;
        // 波特率
        baudRate = cpara.BRate;
        // 串口名
        uartpath = cpara.ComName;

        try {
            mSerialPort = new SerialPort();
            mfd = mSerialPort.open(uartpath, baudRate, dataBit, parity, stopBit);
            mOutputStream = mSerialPort.getOutputStream(mfd);
            mInputStream = mSerialPort.getInputStream(mfd);
            aOpenStatus = true;
        } catch (Exception ex) {
            EventBus.getDefault().post(new EventMsg("OpenPort failed:" + uartpath+","+baudRate+","+dataBit+","+parity+","+stopBit));
            return false;
        }
        EventBus.getDefault().post(new EventMsg("OpenPort:" + uartpath+","+baudRate+","+dataBit+","+parity+","+stopBit));
        return true;
    }

    @Override
    public boolean closeDevice() {
        try {
            if (mSerialPort != null) {
                aOpenStatus = false;
                mInputStream = null;
                mOutputStream = null;
                mSerialPort.Close();
            }
        } catch (Exception ex) {
            return false;
        }
        EventBus.getDefault().post(new EventMsg("ClosePort:" + uartpath+","+baudRate+","+dataBit+","+parity+","+stopBit));
        return true;
    }

    @Override
    public byte[] receiveBytToCallback(int waitT, int byteLen) {
        byte[] rtnByt = new byte[600];
        byte[] rBuffer = new byte[600];
        byte[] byteData = new byte[0];
        delay_occurZJ = false;
        int size;
        int index = 0;
        startTimer(waitT);
        try {
            while (!delay_occurZJ) {
                if (mInputStream.available() > 0) {
                    size = mInputStream.read(rBuffer);// 刚开始为-1
                    if (size > 0) {
                        for (int i = 0; i < size; i++) {
                            rtnByt[index] = rBuffer[i];
                            index++;
                        }
                        if (index >= byteLen) {
                            delay_occurZJ = true;
                            break;
                        }


                        /**
                         * 验证是否 8D0A 结束
                         * 握手帧和波特率 该标志作为结束
                         */
                        if (index > 1 && rtnByt[index - 1] == 0x0a) {
                            if ((rtnByt[index - 2] & 0xff) == 0x8d) {
                                delay_occurZJ = true;
                                System.out.println(TAG + "||满足8D0A结束");
                                break;
                            }
                        }

                        /**
                         * 正常 以 7E开头 7E结束 byte[] 下标2 代表字节长度
                         */

                        //System.out.println(TAG + "||" + (rtnByt[index - 1] & 0xff) + "||" + ((rtnByt[index - 1] & 0xff) == 0x7e));
                        if (index > 1 && (rtnByt[index - 1] & 0xff) == 0x7e) {
                            byteData = Arrays.copyOf(rtnByt, index);
                            if (byteData.length > 2) {
                                int hexLen = byteData[2] & 0xff;
                                int recLen = (byteData.length - 2) & 0xff;
                                //System.out.println(TAG + "||" + hexLen + "||" + recLen);
                                if (hexLen == recLen) {
                                    delay_occurZJ = true;
                                    // System.out.println(TAG + "||满足7E开头7E结束");
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            if (index > 0) {
                byteData = Arrays.copyOf(rtnByt, index);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            stopTimer();
        }
        return byteData;
    }

    @Override
    public byte[] receiveByt(int SleepT, int witT) {
        return receiveByt(SleepT, witT, 0);
    }

    @Override
    public byte[] receiveByt(int SleepT, int WaitT, int byteLen) {
        byte[] rtnByt = new byte[600];
        byte[] rBuffer = new byte[600];
        byte[] byteData = new byte[0];
        delay_occurZJ = false;
        int size;
        int Index = 0;
        SystemClock.sleep(SleepT);
        startTimer(WaitT);
        try {
            while (!delay_occurZJ) {
                SystemClock.sleep(200);
                if (mInputStream.available() > 0) {
                    size = mInputStream.read(rBuffer);// 刚开始为-1
                    if (size > 0) {
                        //System.out.println("test Rec:" + HexStringUtil.bytesToHexString(rBuffer));
                        for (int i = 0; i < size; i++) {
                            if (Index == 0 && (rBuffer[0] == 0x00)) {
                                //去除 字节 0 的数据
                                rBuffer = HexStringUtil.removeBytes(rBuffer, 0, 0);
                                size -= 1;
                                i -= 1;
                            } else {
                                rtnByt[Index] = rBuffer[i];
                                Index++;
                            }
                        }

                        if (byteLen > 0 && Index >= byteLen) {
                            delay_occurZJ = true;
                            stopTimer();
                            System.out.println(TAG + "||满足字节size结束");
                            break;
                        }

                        if (Index > 3) {
                            int len;
                            for (int m = 0; m < Index; m++) {
                                if (((rtnByt[m] & 0xff) == 0x68 && (rtnByt[m + 1] & 0xff) == 0x81)
                                        || ((rtnByt[m] & 0xff) == 0x68 && (rtnByt[m + 1] & 0xff) == 0xA1)
                                        || ((rtnByt[m] & 0xff) == 0x68 && (rtnByt[m + 1] & 0xff) == 0x9F)
                                        || ((rtnByt[m] & 0xff) == 0x68 && (rtnByt[m + 1] & 0xff) == 0x84)) {
                                    len = rtnByt[m + 2] & 0xff;
                                    if (Index - m - 2 >= len) {
                                        delay_occurZJ = true;
                                        stopTimer();
                                        break;
                                    }
                                }
                            }
                        }
                    }

                }
            }

            if (Index > 0) {
                byteData = Arrays.copyOf(rtnByt, Index);
                String res = HexStringUtil.bytesToHexString(byteData);
                if (HexClient645API.getDebugMode()) {
                    System.out.println("Rec:" + res);
                    EventBus.getDefault().post(new EventMsg("Rec:" + res));

                }
            }


        } catch (Exception ex) {
            System.out.println("Error:" + ex.getMessage());
        } finally {
            stopTimer();
        }

        return byteData;
    }


    /**
     * 数据接收
     *
     * @param waitTime    超时接收时间
     * @param checkFilter 过滤类型
     * @param byteLen     帧长度
     * @return byte[]
     */
    public byte[] receiveByte(int waitTime, int checkFilter, int byteLen) {
        byte[] rtnByt = new byte[600];
        byte[] rBuffer = new byte[600];
        byte[] byteData = new byte[0];
        delay_occurZJ = false;
        int size;
        int Index = 0;
        startTimer(waitTime);
        try {

            while (!delay_occurZJ) {
                if (mInputStream.available() > 0) {
                    size = mInputStream.read(rBuffer);// 刚开始为-1

                    if (size > 0) {
                        for (int i = 0; i < size; i++) {
                            rtnByt[Index] = rBuffer[i];
                            Index++;
                        }

                        if (byteLen > 0 && Index >= byteLen) {
                            delay_occurZJ = true;
                            System.out.println(TAG + "||满足字节size结束");
                            break;
                        }
                        /**
                         * 验证是否 8D0A 结束
                         * 握手帧和波特率 该标志作为结束
                         */
                        if (Index > 1 && rtnByt[Index - 1] == 0x0a) {
                            if ((rtnByt[Index - 2] & 0xff) == 0x8d) {
                                delay_occurZJ = true;
                                System.out.println(TAG + "||满足8D0A结束");
                                break;
                            }
                        }
                    }

                }
            }

            if (Index > 0) {
                byteData = Arrays.copyOf(rtnByt, Index);
                String res = HexStringUtil.bytesToHexString(byteData);
                if (HexClient645API.getDebugMode()) {
                    System.out.println("Rec:" + res);
                    EventBus.getDefault().post(new EventMsg("Rec:" + res));

                }
            }


        } catch (Exception ex) {
            System.out.println("Error:" + ex.getMessage());
        } finally {
            stopTimer();
        }

        return byteData;
    }


    /**
     * 数据接收
     *
     * @param maxWaitTime int    超时接收时间
     * @return byte[]
     */
    public byte[] receiveByte(int maxWaitTime) {
        byte[] rtnByt = new byte[600];
        byte[] rBuffer = new byte[600];
        byte[] byteData = new byte[0];
        delay_occurZJ = false;
        int size;
        int Index = 0;
        startTimer(maxWaitTime);
        try {

            while (!delay_occurZJ) {
                if (mInputStream.available() > 0) {
                    size = mInputStream.read(rBuffer);// 刚开始为-1

                    if (size > 0) {
                        for (int i = 0; i < size; i++) {
                            rtnByt[Index] = rBuffer[i];
                            Index++;
                        }
                    }

                }
            }

            if (Index > 0) {
                byteData = Arrays.copyOf(rtnByt, Index);
                String res = HexStringUtil.bytesToHexString(byteData);
                System.out.println("Rec:" + res);
                EventBus.getDefault().post(new EventMsg("Rec:" + res));

            }


        } catch (Exception ex) {
            System.out.println("Error:" + ex.getMessage());
        } finally {
            stopTimer();
        }

        return byteData;
    }


    @Override
    public boolean sendByt(byte[] sndByte) {
        // TODO Auto-generated method stub
        boolean result = false;
        if (!aOpenStatus || mOutputStream == null) {
            try {
                mSerialPort = new SerialPort();
                mfd = mSerialPort.open(HexDevice.COMM_NAME_ZIGBEE, 4800, 8, 'N', 1);
                mOutputStream = mSerialPort.getOutputStream(mfd);
                mInputStream = mSerialPort.getInputStream(mfd);
                aOpenStatus = true;
                SystemClock.sleep(20);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                return false;
            }
        }
        try {
            mOutputStream.write(sndByte, 0, sndByte.length);// write(sndByte);
            result = true;
            String res = HexStringUtil.bytesToHexString(sndByte);
            System.out.println("Send:" + res);
            EventBus.getDefault().post(new EventMsg("Send:" + res));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void setBaudRate(int baudRate) {
        //mSerialPort.close();
        this.baudRate = baudRate;
        mfd = mSerialPort.open(uartpath, this.baudRate, dataBit, parity, stopBit);
        mOutputStream = mSerialPort.getOutputStream(mfd);
        mInputStream = mSerialPort.getInputStream(mfd);
    }

    @Override
    public void setBaudRate(int baudRate, char parity, int dataBit, int stopBit) {
        // mSerialPort.close();
        this.baudRate = baudRate;
        mfd = mSerialPort.open(uartpath, this.baudRate, dataBit, parity, stopBit);
        mOutputStream = mSerialPort.getOutputStream(mfd);
        mInputStream = mSerialPort.getInputStream(mfd);
    }

    public void startTimer(final int time) {
        stopTimer();
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                delay_occurZJ = true;
            }
        };
        timer.schedule(timerTask, time);
    }

    /**
     * 停止 定时器
     */
    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }


    public byte[] PreHandleSendData(byte[] sendData) {
        for (int i = 0; i < sendData.length; i++) {
            sendData[i] += 0x33;
        }
        return sendData;
    }

    public byte[] PreHandleReceiveData(byte[] receivedData) {
        for (int i = 0; i < receivedData.length; i++) {
            receivedData[i] -= 0x33;
        }
        return receivedData;
    }
}
