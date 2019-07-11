package cn.hexing.iec21.comm;

import android.os.SystemClock;

import com.android.SerialPort.SerialPort;

import org.greenrobot.eventbus.EventBus;

import java.io.FileDescriptor;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import cn.hexing.EventMsg;
import cn.hexing.HexStringUtil;
import cn.hexing.iComm.AbsCommAction;
import cn.hexing.model.CommPara;


/**
 * @author cbl
 * @version 2.0
 *          desc: 光电通讯类
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
    private static boolean aOpenStatus = false;

    @Override
    public boolean openDevice(CommPara para) {
        // 停止位
        stopBit = para.Sbit;
        // 校验位
        parity = para.Pty;
        // 数据位
        dataBit = para.DBit;
        // 波特率
        baudRate = para.BRate;
        // 串口名
        uartpath = para.ComName;

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
                mSerialPort.Close();
            }
        } catch (Exception ex) {
            return false;
        }
        EventBus.getDefault().post(new EventMsg("ClosePort:" + uartpath+","+baudRate+","+dataBit+","+parity+","+stopBit));
        return true;
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
            System.out.println("Send:" + HexStringUtil.bytesToHexString(sndByte) + "||" + Arrays.toString(sndByte));
            EventBus.getDefault().post(new EventMsg("Send:"+ HexStringUtil.bytesToHexString(sndByte) + "||" + Arrays.toString(sndByte)));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public byte[] receiveByt(int byteWaitT, int witT) {
        byte[] rtnByt = new byte[600];
        byte[] rBuffer = new byte[600];
        byte[] byteData = new byte[0];
        delay_occurZJ = false;
        int size;
        int Index = 0;

        startTimer(witT);
        try {
            while (!delay_occurZJ) {
                if (mInputStream.available() > 0) {
                    size = mInputStream.read(rBuffer);// 刚开始为-1

                    if (size > 0) {
                        for (int i = 0; i < size; i++) {
                            rtnByt[Index] = rBuffer[i];
                            Index++;
                        }

                        /**
                         * 验证是否 0D0A 结束
                         * 握手帧和波特率 该标志作为结束
                         */
                        if (Index > 1 && rtnByt[Index - 1] == 0x0A) {
                            delay_occurZJ = true;
                            System.out.println(TAG + "||满足0A结束");
                            break;
                        } else if (Index > 2 && rtnByt[0] == 0x01 && rtnByt[Index - 2] == 0x03) {
                            delay_occurZJ = true;
                            System.out.println(TAG + "||满足0150   03 密码帧结束");
                            break;
                        } else if (Index > 1 && rtnByt[0] == 0x7F && (rtnByt[Index - 1] == 0x15 || rtnByt[Index - 1] == 0x06)) {
                            delay_occurZJ = true;
                            System.out.println(TAG + "||满足7F   06或15 密码验证");
                            break;
                        } else if (Index > 3 && rtnByt[Index - 3] == 0x29 && (rtnByt[Index - 2] == 0x03 || rtnByt[Index - 2] == 0x04)) {
                            delay_occurZJ = true;
                            System.out.println(TAG + "||满足29 03或 29 04 数据帧结束");
                            break;
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
    public byte[] receiveByt(int SleepT, int WaitT, int byteLen) {
        byte[] rtnByt = new byte[600];
        byte[] rBuffer = new byte[600];
        byte[] byteData = new byte[0];
        delay_occurZJ = false;
        int size;
        int Index = 0;
        startTimer(WaitT);
        SystemClock.sleep(SleepT);
        try {
            while (!delay_occurZJ) {
                if (mInputStream.available() > 0) {
                    size = mInputStream.read(rBuffer);// 刚开始为-1

                    if (size > 0) {
                        for (int i = 0; i < size; i++) {
                            rtnByt[Index] = rBuffer[i];
                            Index++;
                        }

                        /**
                         * 验证是否 0D0A 结束
                         * 握手帧和波特率 该标志作为结束
                         */
                        if (Index > 1 && rtnByt[Index - 1] == 0x0A) {
                            delay_occurZJ = true;
                            System.out.println(TAG + "||满足0A结束");
                            break;
                        } else if (Index > 2 && rtnByt[0] == 0x01 && rtnByt[Index - 2] == 0x03) {
                            delay_occurZJ = true;
                            System.out.println(TAG + "||满足0150   03 密码帧结束");
                            break;
                        } else if (Index > 1 && rtnByt[0] == 0x7F && (rtnByt[Index - 1] == 0x15 || rtnByt[Index - 1] == 0x06)) {
                            delay_occurZJ = true;
                            System.out.println(TAG + "||满足7F   06或15 密码验证");
                            break;
                        } else if (Index > 3 && rtnByt[Index - 3] == 0x29 && (rtnByt[Index - 2] == 0x03 || rtnByt[Index - 2] == 0x04)) {
                            delay_occurZJ = true;
                            System.out.println(TAG + "||满足29 03或 29 04 数据帧结束");
                            break;
                        } else if (byteLen > 0 && Index >= byteLen) {
                            delay_occurZJ = true;
                            System.out.println(TAG + "||满足字节" + byteLen + " 数据帧结束");
                            break;
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
    public byte[] receiveByt(int sleepT, int waitT, boolean isNeedCon) {
        byte[] rtnByt = new byte[600];
        byte[] rBuffer = new byte[600];
        byte[] byteData = new byte[0];
        delay_occurZJ = false;
        int size;
        int Index = 0;
        SystemClock.sleep(sleepT);
        startTimer(waitT);

        try {
            while (!delay_occurZJ) {
                //SystemClock.sleep(sleepT);
                if (mInputStream.available() > 0) {
                    size = mInputStream.read(rBuffer);// 刚开始为-1

                    if (size > 0) {
                        for (int i = 0; i < size; i++) {
                            rtnByt[Index] = rBuffer[i];
                            Index++;
                        }

                        if (isNeedCon) {
                            for (int k = 0; k < rtnByt.length; k++) {
                                rtnByt[k] = (byte) (rtnByt[k] & 0x7F);
                            }
                        }

                        /**
                         * 验证是否 0D0A 结束
                         * 握手帧和波特率 该标志作为结束
                         */
                        if (Index > 1 && rtnByt[Index - 1] == 0x0A) {
                            delay_occurZJ = true;
                            System.out.println(TAG + "||满足0A结束");
                            break;
                        } else if (Index > 2 && rtnByt[0] == 0x01 && rtnByt[Index - 2] == 0x03) {
                            delay_occurZJ = true;
                            System.out.println(TAG + "||满足0150   03 密码帧结束");
                            break;
                        } else if (Index > 1 && rtnByt[0] == 0x7F && (rtnByt[Index - 1] == 0x15 || rtnByt[Index - 1] == 0x06)) {
                            delay_occurZJ = true;
                            System.out.println(TAG + "||满足7F   06或15 密码验证");
                            break;
                        } else if (Index > 3 && rtnByt[Index - 3] == 0x29 && (rtnByt[Index - 2] == 0x03 || rtnByt[Index - 2] == 0x04)) {
                            delay_occurZJ = true;
                            System.out.println(TAG + "||满足29 03或 29 04 数据帧结束");
                            break;
                        }else   if (Index > 1 && rtnByt[Index - 3] == 0x0A ) {
                            delay_occurZJ = true;
                            System.out.println(TAG + "||满足0A结束");
                            break;
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
    public void setBaudRate(int baudRate) {
        //mSerialPort.close();
        this.baudRate = baudRate;
        mfd = mSerialPort.open(uartpath, this.baudRate, dataBit, parity, stopBit);
        mOutputStream = mSerialPort.getOutputStream(mfd);
        mInputStream = mSerialPort.getInputStream(mfd);
    }

    @Override
    public void setBaudRate(int baudRate, char parity, int dataBit, int stopBit) {

    }

    @Override
    public void setDBitAndParity(int dataBit, char parity) {
        mSerialPort.close();
        // 校验位
        this.parity = parity;
        // 数据位
        this.dataBit = dataBit;
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
