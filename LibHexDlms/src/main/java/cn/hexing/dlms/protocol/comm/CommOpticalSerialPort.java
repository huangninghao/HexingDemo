package cn.hexing.dlms.protocol.comm;


import cn.hexing.HexStringUtil;
import cn.hexing.iComm.AbsCommAction;
import cn.hexing.dlms.IHexListener;
import cn.hexing.model.CommPara;

import com.android.SerialPort.SerialPort;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;


/**
 * @author 王昌豹
 * @version 1.0
 *          光电通讯类
 *          光电头与表通讯，300波特率握手再转高波特率
 *          Copyright (c) 2016
 *          杭州海兴电力科技
 */
public class CommOpticalSerialPort extends AbsCommAction {
    private final static String TAG = CommOpticalSerialPort.class.getSimpleName();
    FileDescriptor mfd;
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
    private IHexListener listener;
    private static boolean aOpenStatus = false;

    public void addListener(IHexListener listener) {
        this.listener = listener;
    }

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
            return false;
        }
        return true;
    }

    @Override
    public boolean closeDevice() {
        try {
            if (mSerialPort != null) {
                aOpenStatus = false;
                mSerialPort.Close();
            }
        } catch (Exception ex) {
            return false;
        }
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
        startTimer(WaitT);
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

                        /**
                         * 正常 以 7E开头 7E结束 byte[] 下标2 代表字节长度
                         */

                        if (Index > 1 && (rtnByt[Index - 1] & 0xff) == 0x7e) {
                            byteData = Arrays.copyOf(rtnByt, Index);
                            if (byteData.length > 2) {
                                int hexLen = byteData[2] & 0xff;
                                int recLen = (byteData.length - 2) & 0xff;
                                //System.out.println(TAG + "||" + hexLen + "||" + recLen);
                                if (hexLen == recLen) {
                                    delay_occurZJ = true;
                                    System.out.println(TAG + "||满足7E开头7E结束");
                                    break;
                                }
                            }
                        }

                    }

                }
            }

            if (Index > 0) {
                byteData = Arrays.copyOf(rtnByt, Index);
                String res = HexStringUtil.bytesToHexString(byteData);
                System.out.println("Rec:" + res);
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
        if (!aOpenStatus) {
            return false;
        }
        try {
            mOutputStream.write(sndByte, 0, sndByte.length);// write(sndByte);
            result = true;
            String res = HexStringUtil.bytesToHexString(sndByte);
            System.out.println("Send:" + res);
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

}
