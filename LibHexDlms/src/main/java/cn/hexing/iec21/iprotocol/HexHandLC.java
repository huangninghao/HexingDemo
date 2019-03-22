package cn.hexing.iec21.iprotocol;

import android.os.SystemClock;

import java.util.ArrayList;

import cn.hexing.HexDevice;
import cn.hexing.HexHandType;
import cn.hexing.HexStringUtil;
import cn.hexing.iComm.AbsCommAction;
import cn.hexing.model.HXFramePara;
import cn.hexing.model.TranXADRAssist;

/**
 * @author cbl
 * @version 2.0
 * @Title: 协议类
 * @Description: 协议，读、写、执行电表
 * @Copyright: Copyright (c) 2016
 * @Company 杭州海兴电力科技
 */
public class HexHandLC implements IProtocol {

    // 发送计数 SSS
    private int Nsend = 0;
    // 接收计数 RRR
    private int Nrec = 0;
    int frameCnt = 0;
    HXHdlcFrame hdlcframe = new HXHdlcFrame();
    // 转换的波特率
    private int ToBaudTate = 300;
    private HXFramePara commParaModel;

    /**
     * -握手
     *
     * @param paraModel  HXFramePara
     * @param commDevice ICommAction
     * @return boolean
     */
    private boolean Handclasp(HXFramePara paraModel, AbsCommAction commDevice) {
        try {
            Nrec = 0;
            Nsend = 0;
            paraModel.Nrec = Nrec;
            paraModel.Nsend = Nsend;
            byte[] sndByt = HexStringUtil.hexToByte(hdlcframe.getBaudRateFrame(paraModel));
            boolean isSend = commDevice.sendByt(sndByt);
            // 检查是否发送成功，发送成功
            if (!isSend) {
                paraModel.ErrTxt = "Serial port access denied!";// 返回错误代码，串口打开失败
                return false;
            }
            byte[] receiveByt = commDevice.receiveByt(paraModel.getHandWaitTime(), paraModel.getDataFrameWaitTime(), paraModel.recDataConversion);
            if (receiveByt != null && receiveByt.length > 1) {
                if (receiveByt.length > 6) {
                    // 发送Z字
                    String z = HexStringUtil.byteToString(receiveByt[4]);
                    paraModel.setZWord(z);
                    sndByt = HexStringUtil.hexToByte(hdlcframe.getSynchronizationFrame(paraModel));
                    isSend = commDevice.sendByt(sndByt);
                    if (!isSend) {
                        // 返回错误代码，串口打开失败
                        paraModel.ErrTxt = "Serial port access denied!";
                        return false;
                    }
                    // 读Z字
                    ToBaudTate = strToIntBaud(HexStringUtil.convertHexToString(z));
                    SystemClock.sleep(paraModel.sleepChangeBaudRate);
                    commDevice.setBaudRate(ToBaudTate);
                    receiveByt = commDevice.receiveByt(paraModel.getHandWaitTime(), paraModel.getDataFrameWaitTime(), paraModel.recDataConversion);
                    if (receiveByt == null || receiveByt.length == 0) {
                        paraModel.ErrTxt = "Over time!";
                        return false;
                    }
                    if (receiveByt.length == 20) {
                        byte[] copy = new byte[12];
                        System.arraycopy(receiveByt, 5, copy, 0, copy.length);
                        paraModel.setMeterPWD(encryptionMeterPwdNew(HexStringUtil.bytesToHexString(copy), paraModel.Pwd));
                    }
                    sndByt = HexStringUtil.hexToByte(hdlcframe.getPwdFrame(paraModel));
                    SystemClock.sleep(paraModel.getSleepT());
                    isSend = commDevice.sendByt(sndByt);
                    if (isSend) {
                        receiveByt = commDevice.receiveByt(paraModel.getHandWaitTime(), paraModel.getDataFrameWaitTime(), paraModel.recDataConversion);
                        if (receiveByt == null || receiveByt.length == 0) {
                            paraModel.ErrTxt = "Set pwd Over time!";
                            return false;
                        }
                        if (receiveByt[receiveByt.length - 1] != (char) 0x06) {
                            //06 = ACK
                            paraModel.ErrTxt = "Set pwd fail!";
                            return false;
                        }
                    } else {
                        paraModel.ErrTxt = "Set pwd send error!";
                        return false;
                    }
                    return true;
                }
            } else {
                paraModel.ErrTxt = "Over time!";
                return false;
            }
        } catch (Exception ex) {
            paraModel.ErrTxt = "Er2:" + ex.getMessage();
            return false;
        }

        return true;
    }

    /**
     * -握手 伊拉克
     *
     * @param paraModel  HXFramePara
     * @param commDevice ICommAction
     * @return boolean
     */
    private boolean HandclaspIraq(HXFramePara paraModel, AbsCommAction commDevice) {
        try {
            Nrec = 0;
            Nsend = 0;
            paraModel.Nrec = Nrec;
            paraModel.Nsend = Nsend;
            byte[] sndByt = HexStringUtil.hexToByte(hdlcframe.getIraqBaudRateFrame(paraModel));
            boolean isSend = commDevice.sendByt(sndByt);
            // 检查是否发送成功，发送成功
            if (!isSend) {
                paraModel.ErrTxt = "Serial port access denied!";// 返回错误代码，串口打开失败
                return false;
            }
            byte[] receiveByt = commDevice.receiveByt(paraModel.getHandWaitTime(), paraModel.getDataFrameWaitTime());
            if (receiveByt != null && receiveByt.length > 1) {

                ////设置模式
                sndByt = HexStringUtil.hexToByte(hdlcframe.getIraqModeFrame(paraModel));
                isSend = commDevice.sendByt(sndByt);
                if (!isSend) {
                    paraModel.ErrTxt = "Serial port access denied!";// 返回错误代码，串口打开失败
                    return false;
                }
                receiveByt = commDevice.receiveByt(paraModel.getHandWaitTime(), paraModel.getDataFrameWaitTime());
                if (receiveByt != null && receiveByt.length > 0) {
                    return true;
                }
            } else {
                paraModel.ErrTxt = "Over time!";
                return false;
            }
        } catch (Exception ex) {
            paraModel.ErrTxt = "Er2:" + ex.getMessage();
            return false;
        }

        return false;
    }


    /**
     * 密码校验
     *
     * @param data 切换波特率回复的数据帧 包含随机数
     * @return 加密运算 密码
     */
    public String encryptionMeterPwd(String data) {
        data = HexStringUtil.convertHexToString(data);
        int RND0, RND1, RND2, RND3, RND4, RND5, RND6;
        RND0 = Integer.valueOf(data.substring(0, 2), 16) & 0xff;
        RND1 = Integer.valueOf(data.substring(2, 4), 16) & 0xff;
        RND2 = Integer.valueOf(data.substring(4, 6), 16) & 0xff;
        RND3 = Integer.valueOf(data.substring(6, 8), 16) & 0xff;
        RND4 = Integer.valueOf(data.substring(8, 10), 16) & 0xff;
        RND5 = Integer.valueOf(data.substring(10, 12), 16) & 0xff;
        RND6 = RND0 + RND1 + RND2 + RND3 + RND4 + RND5;

        int TRND0 = ((RND5 - RND6) ^ RND0) + 0XAA;
        int TRND1 = ((RND4 - RND6) ^ RND1) + 0XAA;
        int TRND2 = RND2 + 0XAA;
        int TRND3 = ((RND2 - RND6) ^ RND3) + 0XAA;
        int TRND4 = ((RND1 - RND6) ^ RND4) + 0XAA;
        int TRND5 = ((RND0 - RND6) ^ RND5) + 0XAA;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("%02x", (TRND0 & 0X0F) * 16 + ((TRND0 & 0XF0) / 16)).toUpperCase());
        stringBuilder.append(String.format("%02x", (TRND1 & 0X0F) * 16 + ((TRND1 & 0XF0) / 16)).toUpperCase());
        stringBuilder.append(String.format("%02x", (TRND2 & 0X0F) * 16 + ((TRND2 & 0XF0) / 16)).toUpperCase());
        stringBuilder.append(String.format("%02x", (TRND3 & 0X0F) * 16 + ((TRND3 & 0XF0) / 16)).toUpperCase());
        stringBuilder.append(String.format("%02x", (TRND4 & 0X0F) * 16 + ((TRND4 & 0XF0) / 16)).toUpperCase());
        stringBuilder.append(String.format("%02x", (TRND5 & 0X0F) * 16 + ((TRND5 & 0XF0) / 16)).toUpperCase());

        return stringBuilder.toString().toUpperCase();
    }

    /**
     * 密码校验
     *
     * @param randomNum 切换波特率回复的数据帧 包含随机数
     * @return 加密运算 密码
     */
    public String encryptionMeterPwdNew(String randomNum, String meterPwd) {
        if (meterPwd.length() != 12) {
            return meterPwd;
        }
        String password = meterPwd;//"000000000000";
        byte rnd6 = 0x00;
        String enPassWord = "";
        String tmpPassWord = "";
        randomNum = HexStringUtil.convertHexToString(randomNum);
        System.out.println("rec=" + randomNum.toUpperCase());
        for (int i = 0; i < 6; i++) {
            rnd6 = (byte) ((rnd6 + Integer.valueOf(randomNum.substring(i * 2, (i + 1) * 2), 16)) > 0x100 ?
                    (rnd6 + Integer.valueOf(randomNum.substring(i * 2, (i + 1) * 2), 16) - 0x100) :
                    (rnd6 + Integer.valueOf(randomNum.substring(i * 2, (i + 1) * 2), 16)));
        }
        for (int i = 0; i < 6; i++) {
            byte a = (byte) (Integer.valueOf(randomNum.substring((5 - i) * 2, (5 - i + 1) * 2), 16) ^ Integer.valueOf(password.substring(i * 2, (i + 1) * 2), 16));
            byte newByte = (byte) (a >= rnd6 ? (a - rnd6) : a + 0x100 - rnd6);
            String byteStr = String.format("%02x", newByte);
            if (i == 2) {
                tmpPassWord += password.substring(4, 6);
            } else {
                tmpPassWord += byteStr;
            }
        }
        for (int i = 0; i < 6; i++) {
            byte a = (byte) (Integer.valueOf(randomNum.substring(i * 2, (i + 1) * 2), 16) ^ Integer.valueOf(tmpPassWord.substring(i * 2, (i + 1) * 2), 16));
            byte newByte = (byte) (a > 0x55 ? a + 0xAA - 0x100 : a + 0xAA);
            String byteStr = String.format("%02x", newByte);
            enPassWord += byteStr.substring(1) + byteStr.substring(0, 1);
        }
        return enPassWord.toUpperCase();
    }

    /**
     * DLMS-None身份验证
     *
     * @param para       HXFramePara
     * @param commDevice ICommAction
     * @return
     */
    private boolean LinkNoAuth(HXFramePara para, AbsCommAction commDevice) {
        byte[] sndByt = null;
        byte[] receiveByt = null;
        boolean isSend = false;
        Nrec = 0;
        Nsend = 0;
        para.Nrec = Nrec;
        para.Nsend = Nsend;
        //sndByt = hdlcframe.getNoAuthSNRMFrame(para);
        isSend = commDevice.sendByt(sndByt);
        if (!isSend) {
            // 协议提示通讯错误，通讯日志记录各类错误
            para.ErrTxt = "DLMS_SNRM_FAILED";
            return false;
        } else {
        }
        receiveByt = commDevice.receiveByt(para.ByteWaitT, para.dataFrameWaitTime, para.recDataConversion);
        if (receiveByt != null) {
            if (!checkFrame(receiveByt, false, para)) {
                return false;
            }
            getLinkPara(receiveByt, para);
        } else {
            para.ErrTxt = "DLMS_OVER_TIME";
            return false;
        }
        // sndByt = hdlcframe.getNoAuthAARQFrame(para);
        isSend = commDevice.sendByt(sndByt);

        if (!isSend) {
            para.ErrTxt = "DLMS_AARQ_FAILED";
            return false;
        } else {
        }
        Nsend = 1;
        para.Nsend = Nsend;
        receiveByt = commDevice.receiveByt(para.SleepT, para.dataFrameWaitTime, para.recDataConversion);
        if (receiveByt != null) {
            if (!checkFrame(receiveByt, true, para)) {
                para.ErrTxt = "DLMS_FORMAT_ERROR";
                return false;
            }
            Nrec = 1;
            para.Nrec = 1;
        } else {
            para.ErrTxt = "DLMS_OVER_TIME";
            return false;
        }
        return true;
    }

    /**
     * LLS身份认证
     *
     * @param para       HXFramePara
     * @param commDevice ICommAction
     * @return boolean
     */
    private boolean LinkLLSAuth(HXFramePara para, AbsCommAction commDevice) {
        byte[] sndByt = null;
        byte[] receiveByt = null;
        boolean isSend = false;
        Nrec = 0;
        Nsend = 0;
        // sndByt = hdlcframe.getNoAuthSNRMFrame(para);
        isSend = commDevice.sendByt(sndByt);
        if (!isSend) {
            para.ErrTxt = "DLMS_SNRM_FAILED";
            return false;
        }
        receiveByt = commDevice.receiveByt(para.ByteWaitT, para.dataFrameWaitTime, para.recDataConversion);
        if (receiveByt != null) {
            if (!checkFrame(receiveByt, false, para)) {
                return false;
            }
            getLinkPara(receiveByt, para);

        } else {
            para.ErrTxt = "DLMS_OVER_TIME";
            return false;
        }
        // sndByt = hdlcframe.getLLSAuthAARQFrame(para);
        SystemClock.sleep(200);
        isSend = commDevice.sendByt(sndByt);
        if (!isSend) {
            para.ErrTxt = "DLMS_AARQ_FAILED";
            return false;
        }
        Nsend = 1;
        para.Nsend = Nsend;
        receiveByt = commDevice.receiveByt(para.ByteWaitT, para.dataFrameWaitTime, para.recDataConversion);
        if (receiveByt != null) {
            if (!checkFrame(receiveByt, true, para)) {
                return false;
            }
            Nrec = 1;
            para.Nrec = Nrec;

        } else {

            para.ErrTxt = "DLMS_OVER_TIME";
            return false;
        }
        return true;
    }

    /**
     * DLMS-HLS身份认证
     *
     * @param para       HXFramePara
     * @param commDevice ICommAction
     * @return boolean
     */
    private boolean linkHLSAuth(HXFramePara para, AbsCommAction commDevice) {
        byte[] sndByt = null;
        byte[] receiveByt = null;
        boolean isSend = false;
        Nrec = 0;
        Nsend = 0;
        // sndByt = hdlcframe.getNoAuthSNRMFrame(para);
        isSend = commDevice.sendByt(sndByt);
        if (!isSend) {
            para.ErrTxt = "DLMS_SNRM_FAILED";
            return false;
        }
        receiveByt = commDevice.receiveByt(para.ByteWaitT, para.dataFrameWaitTime, para.recDataConversion);
        if (receiveByt != null && receiveByt.length > 0) {
            if (!checkFrame(receiveByt, false, para)) {
                return false;
            }
            getLinkPara(receiveByt, para);
        } else {
            para.ErrTxt = "DLMS_OVER_TIME";
            return false;
        }
        frameCnt = 1;
        para.frameCnt = frameCnt;
        // sndByt = hdlcframe.getHLSAuthAARQFrame(para);
        SystemClock.sleep(200);
        isSend = commDevice.sendByt(sndByt);
        if (!isSend) {
            para.ErrTxt = "DLMS_AARQ_FAILED";
            return false;
        }
        Nsend = 1;
        para.Nsend = 1;
        receiveByt = commDevice.receiveByt(para.ByteWaitT, para.dataFrameWaitTime, para.recDataConversion);
        if (receiveByt != null && receiveByt.length > 0) {
            if (!checkFrame(receiveByt, true, para)) {
                return false;
            }
            Nrec = 1;
            para.Nrec = Nrec;
            // 提取随机数，对其加密
            para.frameCnt = frameCnt;
            HXFramePara paraModelCopy = new HXFramePara();
            copyPara(paraModelCopy, para);
            paraModelCopy.OBISattri = "000F0000280000FF01";
            paraModelCopy.FirstFrame = false;
            commParaModel.isHands = false;
            String EncodeStr = null;// hdlcframe.getStoc(paraModelCopy, receiveByt);
            paraModelCopy.WriteData = EncodeStr;
            if (!action(paraModelCopy, commDevice)) {
                return false;
            }
        } else {

            para.ErrTxt = "DLMS_OVER_TIME";
            return false;
        }
        return true;
    }

    /**
     * 改变通道
     *
     * @param para       HXFramePara
     * @param commDevice ICommAction
     * @return bool
     */
    private synchronized boolean changeChannel(HXFramePara para, AbsCommAction commDevice) {
        if (para == null) {
            return false;
        }

        boolean isSend = false;// commDevice.sendByt(hdlcframe.getChangeChannel(para));
        if (isSend) {
            byte[] receiveByt = commDevice.receiveByt(para.ByteWaitT, para.dataFrameWaitTime, para.recDataConversion);
            if (receiveByt != null && receiveByt.length > 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized byte[] read(HXFramePara paraModel, AbsCommAction commDevice) {
        commParaModel = paraModel;
        ArrayList<Byte> rtnReceiveByt = new ArrayList<>();
        paraModel.Nsend = Nsend;
        paraModel.Nrec = Nrec;
        if (paraModel.FirstFrame) {
            if (HexDevice.OPTICAL.equals(paraModel.CommDeviceType)) {
                commDevice.setBaudRate(300);
                if (!Handclasp(paraModel, commDevice)) {
                    paraModel.ErrTxt = "DLMS_AUTH_FAILED";
                    return null;
                }
            } else if (HexDevice.RF.equals(paraModel.CommDeviceType)) {
                commDevice.setBaudRate(4800);
                if (paraModel.handType == HexHandType.IRAQ) {
                    if (!HandclaspIraq(paraModel, commDevice)) {
                        paraModel.ErrTxt = "DLMS_AUTH_FAILED";
                        return null;
                    }
                } else {
                    if (!Handclasp(paraModel, commDevice)) {
                        paraModel.ErrTxt = "DLMS_AUTH_FAILED";
                        return null;
                    }
                }
            }

        }
        if (paraModel.isHands) {
            if (paraModel.Mode == HXFramePara.AuthMode.NONE) {
                if (!LinkNoAuth(paraModel, commDevice)) {
                    paraModel.ErrTxt = "DLMS_AUTH_FAILED";
                    return null;
                }
            } else if (paraModel.Mode == HXFramePara.AuthMode.LLS) {
                if (!LinkLLSAuth(paraModel, commDevice)) {
                    paraModel.ErrTxt = "DLMS_AUTH_FAILED";
                    return null;
                }
            } else if (paraModel.Mode == HXFramePara.AuthMode.HLS) {
                if (!linkHLSAuth(paraModel, commDevice)) {
                    paraModel.ErrTxt = "DLMS_AUTH_FAILED";
                    return null;
                }

            }
        }
        frameCnt++;
        paraModel.frameCnt = frameCnt;
        paraModel.Nrec = Nrec;
        paraModel.Nsend = Nsend;
        byte[] sndByt = HexStringUtil.hexToByte(hdlcframe.getReadR2Frame(paraModel));

        SystemClock.sleep(paraModel.getSleepT());

        boolean isSend = commDevice.sendByt(sndByt);
        if (!isSend) {
            paraModel.ErrTxt = "DLMS_NORMAL_FAILED";
            return new byte[0];
        }
        Nsend++;
        paraModel.Nsend = Nsend;
        byte[] receiveByt = commDevice.receiveByt(paraModel.SleepT, paraModel.dataFrameWaitTime, paraModel.recDataConversion);
        if (receiveByt == null || receiveByt.length == 0) {
            return new byte[0];
        }

        Nrec++;
        paraModel.Nrec = Nrec;

        //有没有后续帧 数组倒数第二个byte 为准 03表示 没有后续  04代表有后续帧
        int result = -1;
        int reLength = receiveByt.length;

        if (reLength > 3) {
            result = (receiveByt[receiveByt.length - 2] & 0xff);
        }

        if (result == 0x03) {
            //没有后续帧 数组倒数第二个byte 下标2 开始为数据帧
            for (int i = 0; i < reLength; i++) {
                if ((receiveByt[i] & 0xFF) == 0x02 && (receiveByt[i + 1] & 0xFF) == 0x28) {
                    for (int m = i + 2; m < reLength - 3; m++) {
                        rtnReceiveByt.add(receiveByt[m]);
                    }
                    break;
                }
            }
        }
        if (result == 0x04) {
            //有后续帧 数组倒数第二个byte  下标2 开始为数据帧
            boolean isLastFrame = false;
            for (int i = 0; i < reLength; i++) {
                if ((receiveByt[i] & 0xFF) == 0x02 && (receiveByt[i + 1] & 0xFF) == 0x28) {
                    for (int m = i + 2; m < reLength - 3; m++) {
                        rtnReceiveByt.add(receiveByt[m]);
                    }
                    break;
                }
            }
            while (!isLastFrame) {
                //未完成 数据块解析开发
                isLastFrame = true;
                frameCnt++;
                paraModel.frameCnt = frameCnt;
                paraModel.Nsend = Nsend;
                paraModel.Nrec = Nrec;
                sndByt = HexStringUtil.hexToByte(hdlcframe.getReadRequestBlockFrame(paraModel));
                SystemClock.sleep(paraModel.getSleepT());
                isSend = commDevice.sendByt(sndByt);
                if (!isSend) {
                    paraModel.ErrTxt = "DLMS_BLOCK_FAILED";
                    return null;
                }
                Nsend++;
                paraModel.Nsend = Nsend;
                receiveByt = commDevice.receiveByt(paraModel.SleepT, paraModel.dataFrameWaitTime);
                if (receiveByt != null && receiveByt.length > 0) {
                    System.out.println("数据块接收=" + HexStringUtil.bytesToHexString(receiveByt));
                }
            }
        }
        byte[] bytResult = new byte[rtnReceiveByt.size()];
        for (int i = 0; i < bytResult.length; i++) {
            bytResult[i] = rtnReceiveByt.get(i);
        }
        return bytResult;
    }

    @Override
    public synchronized boolean write(HXFramePara paraModel, AbsCommAction commDevice) {

        commParaModel = paraModel;//
        paraModel.Nsend = Nsend;
        paraModel.Nrec = Nrec;
        byte[] sndByt = null;
        byte[] receiveByt = null;
        boolean isSend = false;
        if (paraModel.FirstFrame) {
            // 光电必须发“/?!”握手
            if (HexDevice.OPTICAL.equals(paraModel.CommDeviceType)) {
                if (!Handclasp(paraModel, commDevice)) {
                    paraModel.ErrTxt = "DLMS_AUTH_FAILED";
                    return false;
                }
            } else if (HexDevice.RF.equals(paraModel.CommDeviceType)) {
                if (!changeChannel(paraModel, commDevice)) {
                    paraModel.ErrTxt = "DLMS_CHANNEL_FAILED";
                    return false;
                }
            }

        }
        if (paraModel.isHands) {
            if (paraModel.Mode == HXFramePara.AuthMode.NONE) {
                if (!LinkNoAuth(paraModel, commDevice)) {
                    paraModel.ErrTxt = "DLMS_AUTH_FAILED";
                    return false;
                }
            }
            if (paraModel.Mode == HXFramePara.AuthMode.LLS) {
                if (!LinkLLSAuth(paraModel, commDevice)) {
                    paraModel.ErrTxt = "DLMS_AUTH_FAILED";
                    return false;
                }
            } else if (paraModel.Mode == HXFramePara.AuthMode.HLS) {
                if (!linkHLSAuth(paraModel, commDevice)) {
                    paraModel.ErrTxt = "DLMS_AUTH_FAILED";
                    return false;
                }
            }
        }
        if ((paraModel.WriteData.length() / 2 + 18) < paraModel.MaxSendInfo_Value) {
            frameCnt++;
            paraModel.frameCnt = frameCnt;
            paraModel.Nsend = Nsend;
            paraModel.Nrec = Nrec;
            sndByt = HexStringUtil.hexToByte(hdlcframe.getWriteFrame(paraModel));
            if (HexDevice.RF.equals(paraModel.CommDeviceType)) {
                SystemClock.sleep(20);
            }
            isSend = commDevice.sendByt(sndByt);
            if (!isSend) {
                paraModel.ErrTxt = "DLMS_WRITE_FAILED";
                return false;
            }
            Nsend++;
            paraModel.Nsend = Nsend;
            receiveByt = commDevice.receiveByt(paraModel.ByteWaitT, paraModel.dataFrameWaitTime, paraModel.recDataConversion);
            if (receiveByt == null || receiveByt.length == 0) {
                paraModel.ErrTxt = "receiver byte is empty";
                return false;
            }
            if (receiveByt[receiveByt.length - 1] != (char) 0x06) {
                //06 != ACK
                paraModel.ErrTxt = "receiver byte is " + (char) receiveByt[receiveByt.length - 1];
                return false;
            }
            return true;
        } else {
            // #region set.request_block
            int BlockNum = 1;
            while (paraModel.WriteData.length() > 0) {
                frameCnt++;
                paraModel.frameCnt = frameCnt;
                paraModel.BlockNum = BlockNum;
                paraModel.Nrec = Nrec;
                paraModel.Nsend = Nsend;
                sndByt = HexStringUtil.hexToByte(hdlcframe.getWriteFrame(paraModel));
                isSend = commDevice.sendByt(sndByt);
                if (!isSend) {
                    paraModel.ErrTxt = "DLMS_BLOCK_FAILED";
                    return false;
                }
                Nsend++;
                paraModel.Nsend = Nsend;
                if (BlockNum == 0xff) {
                }
                BlockNum++;
                receiveByt = commDevice.receiveByt(paraModel.ByteWaitT, paraModel.dataFrameWaitTime, paraModel.recDataConversion);

                if (receiveByt != null && receiveByt.length > 0 && receiveByt[receiveByt.length - 1] == (char) 0x06) {
                    Nrec++;
                    paraModel.Nrec = Nrec;
                } else {
                    paraModel.ErrTxt = "DLMS_OVER_TIME";
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    public synchronized boolean action(HXFramePara paraModel, AbsCommAction commDevice) {
        commParaModel = paraModel;//
        paraModel.Nsend = Nsend;
        paraModel.Nrec = Nrec;

        byte[] sndByt = null;
        byte[] receiveByt = null;
        boolean isSend = false;
        if (paraModel.FirstFrame) {
            if (HexDevice.OPTICAL.equals(paraModel.CommDeviceType)) {
                if (!Handclasp(paraModel, commDevice)) {
                    paraModel.ErrTxt = "DLMS_AUTH_FAILED";
                    return false;
                }
            } else if (HexDevice.RF.equals(paraModel.CommDeviceType)) {
                if (paraModel.handType == HexHandType.IRAQ) {
                    if (!HandclaspIraq(paraModel, commDevice)) {
                        paraModel.ErrTxt = "DLMS_AUTH_FAILED";
                        return false;
                    }
                } else {
                    if (!changeChannel(paraModel, commDevice)) {
                        paraModel.ErrTxt = "DLMS_CHANNEL_FAILED";
                        return false;
                    }
                }
            }

        }

        if (paraModel.isHands) {
            if (paraModel.Mode == HXFramePara.AuthMode.NONE) {
                if (!LinkNoAuth(paraModel, commDevice)) {
                    paraModel.ErrTxt = "DLMS_AUTH_FAILED";
                    return false;
                }
            } else if (paraModel.Mode == HXFramePara.AuthMode.HLS) {
                if (!linkHLSAuth(paraModel, commDevice)) {
                    paraModel.ErrTxt = "DLMS_AUTH_FAILED";
                    return false;
                }
            }
        }
        // action.request_normal
        frameCnt++;
        paraModel.frameCnt = frameCnt;
        paraModel.Nsend = Nsend;
        paraModel.Nrec = Nrec;
        sndByt = HexStringUtil.hexToByte(hdlcframe.getActionFrame(paraModel));
        if (HexDevice.RF.equals(paraModel.CommDeviceType)) {
            SystemClock.sleep(20);
        }
        isSend = commDevice.sendByt(sndByt);
        if (!isSend) {
            paraModel.ErrTxt = "DLMS_ACTION_FAILED";
            return false;
        }
        Nsend++;
        paraModel.Nsend = Nsend;
        receiveByt = commDevice.receiveByt(paraModel.ByteWaitT, paraModel.dataFrameWaitTime, paraModel.recDataConversion);
        if (receiveByt != null && receiveByt.length > 0) {
            Nrec++;
            paraModel.Nrec = Nrec;
        } else {
            paraModel.ErrTxt = "DLMS_OVER_TIME";
            return false;
        }
        if (paraModel.enLevel != 0x00)// 如果有加密或认证，则将收到数据还原
        {
            // receiveByt = hdlcframe.getOriginalData(receiveByt, paraModel);
        }
        if (receiveByt[10 + paraModel.DestAddr.length] == 0xd8) {
            paraModel.ErrTxt = "DLMS_SER_NOTALLOWED";
            return false;
        }
        if (receiveByt[13 + paraModel.DestAddr.length] != 0x00) {
            paraModel.ErrTxt = accessResult(receiveByt[13 + paraModel.DestAddr.length]);
            return false;
        }
        return true;
    }


    /**
     * 直接发送 byte[] 数据
     *
     * @param paraModel  HXFramePara
     * @param commDevice ICommAction
     * @param assist     TranXADRAssist
     * @return TranXADRAssist
     */
    @Override
    public TranXADRAssist sendByte(HXFramePara paraModel, AbsCommAction commDevice, TranXADRAssist assist) {
        if (assist.isFirstFrame) {
            if (HexDevice.OPTICAL.equals(paraModel.CommDeviceType)) {
                if (!Handclasp(paraModel, commDevice)) {
                    paraModel.ErrTxt = "DLMS_AUTH_FAILED";
                    assist.errMsg = "DLMS_AUTH_FAILED";
                    assist.aResult = false;
                    return assist;
                }
            } else if (HexDevice.RF.equals(paraModel.CommDeviceType)) {
                if (!changeChannel(paraModel, commDevice)) {
                    paraModel.ErrTxt = "DLMS_CHANNEL_FAILED";
                    assist.errMsg = "DLMS_CHANNEL_FAILED";
                    assist.aResult = false;
                    return assist;
                }
            }
        }

        if (assist.isHands) {
            if (paraModel.Mode == HXFramePara.AuthMode.NONE) {
                if (!LinkNoAuth(paraModel, commDevice)) {
                    paraModel.ErrTxt = "DLMS_AUTH_FAILED";
                    assist.errMsg = "DLMS_AUTH_FAILED";
                    assist.aResult = false;
                    return assist;
                }
            } else if (paraModel.Mode == HXFramePara.AuthMode.HLS) {
                if (!linkHLSAuth(paraModel, commDevice)) {
                    paraModel.ErrTxt = "DLMS_AUTH_FAILED";
                    assist.errMsg = "DLMS_AUTH_FAILED";
                    assist.aResult = false;
                    return assist;
                }
            }
        }

        boolean isSend = commDevice.sendByt(HexStringUtil.hexToByte(assist.writeData));
        if (!isSend) {
            assist.errMsg = "DLMS_SEND_FAILED";
            return assist;
        }
        if (assist.byteLen > 0) {
            assist.recBytes = commDevice.receiveBytToCallback(paraModel.dataFrameWaitTime, assist.byteLen);
        } else {
            assist.recBytes = commDevice.receiveByt(paraModel.getHandWaitTime(), paraModel.getDataFrameWaitTime(), paraModel.recDataConversion);
        }
        if (assist.recBytes == null || assist.recBytes.length == 0) {
            assist.aResult = false;
        } else {
            assist.recStrData = HexStringUtil.bytesToHexString(assist.recBytes);
            assist.aResult = true;
        }
        return assist;
    }

    /**
     * 断开链路层
     *
     * @param commDevice 接口对象
     * @return bool
     */
    @Override
    public boolean discFrame(AbsCommAction commDevice) {
        return true;
    }

    /**
     * 断开链路层
     *
     * @param commDevice 接口对象
     * @return bool
     */
    public boolean discFrame(HXFramePara para, AbsCommAction commDevice) {
        if (commDevice == null) {
            return false;
        }
        byte[] TmpArr = HexStringUtil.hexToByte(hdlcframe.getEndFrame(para));
        boolean isSuccess = commDevice.sendByt(TmpArr);
        if (isSuccess) {
            commDevice.receiveByt(300, 1500);
        } else {
            return false;
        }
        return true;
    }

    /**
     * CRC校验和检验
     *
     * @param receiveByt byte[]
     * @return bool
     */
    private boolean checkFrame(byte[] receiveByt) {
        boolean isOK = false;
        if (receiveByt == null) {
            return isOK;
        }
        if (receiveByt.length > 3) {
            StringBuilder stringBuilder = new StringBuilder();
            int i = receiveByt[1] & 0x07;
            stringBuilder.append(i < 10 ? "0" + i : String.valueOf(i));
            stringBuilder.append(HexStringUtil.byteToString(receiveByt[2]));
            int len = Integer.valueOf(stringBuilder.toString(), 16);
            //2个字节表示长度 一个字节8个数据位 第一个字节高位表示数据类型 低位和第二个字节表示长度
            if (len == receiveByt.length - 2) {
                isOK = true;
                byte[] FrameEnd = new byte[0];//测试临时注释//hdlcframe.CRC16(receiveByt, 1, receiveByt.length - 4);
                if (FrameEnd[0] != receiveByt[receiveByt.length - 3]
                        && FrameEnd[1] != receiveByt[receiveByt.length - 2]) {
                    isOK = false;
                }
            }
        }
        return isOK;
    }

    /**
     * 帧长度+CRC校验和检验
     *
     * @param CheckArr byte[]
     * @param CheckNrs bool
     * @param fPara    HXFramePara
     * @return bool
     */
    private boolean checkFrame(byte[] CheckArr, boolean CheckNrs,
                               HXFramePara fPara) {
        // 只检查 长度和HCS
        if (CheckArr.length != (CheckArr[1] & 0xFF & 0x0F) + CheckArr[2] + 2) {
            fPara.ErrTxt = "DLMS_FRAME_NOTVALID";// Message.getCaptionEn("DLMS_FRAME_NOTVALID");
            return false;
        }
        byte[] HCSarr = null;// hdlcframe.CRC16(CheckArr, 1, CheckArr.length - 4);
        if (HCSarr[0] != CheckArr[CheckArr.length - 3]
                || HCSarr[1] != CheckArr[CheckArr.length - 2]) {
            // Error = "Check out error!";
            // return false;
        }
        if (CheckNrs) {
            if (fPara.Nrec >= 8) {
                fPara.Nrec = fPara.Nrec - 8;
            }
            if (fPara.Nsend >= 8) {
                fPara.Nsend = fPara.Nsend - 8;
            }
            byte NsendR = (byte) ((CheckArr[4 + fPara.DestAddr.length] & 0x0E) >> 1);
            byte NrecR = (byte) ((CheckArr[4 + fPara.DestAddr.length] & (byte) 0xE0) >> 5);

            if ((NrecR != fPara.Nsend) || (NsendR != fPara.Nrec)) {
                fPara.ErrTxt = "DLMS_NRNS_ERROR";// Message.getCaptionEn("DLMS_NRNS_ERROR");
                return false;
            }
        }
        return true;
    }

    /**
     * 获取链路协商后的表计链路层参数
     *
     * @param RecPara byte[]
     * @param fPara   HXFramePara
     */
    private void getLinkPara(byte[] RecPara, HXFramePara fPara) {
        int MaxSendInfo_ValueCom = 0x80;
        int MaxRecInfo_ValueCom = 0x80;
        int MaxSendWindow_ValueCom = 0x01;
        int MaxRecWindow_ValueCom = 0x01;
        int TmpVal = 0;
        for (int i = 0; i < RecPara.length; i++) {
            if ((RecPara[i] & 0xff) == 0x81 && (RecPara[i + 1] & 0xff) == 0x80
                    && i < RecPara.length - 2)// 找到81 80
            {
                if (RecPara[i + 2] > 0)// 长度是否大于0
                {
                    int Length = (byte) (RecPara[i + 2]);
                    i = i + 3;
                    if ((RecPara[i] & 0xff) == 0x05 && Length > 0) {
                        byte[] TmpArr = new byte[RecPara[i + 1]];

                        for (int j = 0; j < TmpArr.length; j++) {
                            TmpArr[j] = RecPara[j + i + 2];
                            TmpVal = TmpVal
                                    + (int) ((TmpArr[j] & 0xff) * Math.pow(256,
                                    TmpArr.length - j - 1));
                        }
                        MaxSendInfo_ValueCom = TmpVal;
                        Length = Length - 1 - TmpArr.length;
                        i = i + 2 + TmpArr.length;
                    }
                    if ((RecPara[i] & 0xff) == 0x06 && Length > 0) {
                        byte[] TmpArr = new byte[RecPara[i + 1]];
                        TmpVal = 0;
                        for (int j = 0; j < TmpArr.length; j++) {
                            TmpArr[j] = RecPara[j + i + 2];
                            TmpVal = TmpVal
                                    + (int) ((TmpArr[j] & 0xff) * Math.pow(256,
                                    TmpArr.length - j - 1));
                        }
                        MaxRecInfo_ValueCom = TmpVal;
                        Length = Length - 1 - TmpArr.length;
                        i = i + 2 + TmpArr.length;
                    }
                    if ((RecPara[i] & 0xff) == 0x07 && Length > 0) {
                        byte[] TmpArr = new byte[RecPara[i + 1]];
                        TmpVal = 0;
                        for (int j = 0; j < TmpArr.length; j++) {
                            TmpArr[j] = RecPara[j + i + 2];
                            TmpVal = TmpVal
                                    + (int) ((TmpArr[j] & 0xff) * Math.pow(256,
                                    TmpArr.length - j - 1));
                        }
                        MaxSendWindow_ValueCom = TmpVal;
                        Length = Length - 1 - TmpArr.length;
                        i = i + 2 + TmpArr.length;
                    }
                    // window size_receive
                    if ((RecPara[i] & 0xff) == 0x08 && Length > 0) {
                        byte[] TmpArr = new byte[RecPara[i + 1]];
                        TmpVal = 0;
                        for (int j = 0; j < TmpArr.length; j++) {
                            TmpArr[j] = RecPara[j + i + 2];
                            TmpVal = TmpVal
                                    + (int) ((TmpArr[j] & 0xff) * Math.pow(256,
                                    TmpArr.length - j - 1));
                        }
                        MaxRecWindow_ValueCom = TmpVal;
                        Length = Length - 1 - TmpArr.length;
                        i = i + 2 + TmpArr.length;
                    }
                }
                break;
            }
        }
        fPara.MaxSendInfo_Value = Math.min(fPara.MaxSendInfo_Value,
                MaxRecInfo_ValueCom);
        fPara.MaxRecInfo_Value = Math.min(fPara.MaxRecInfo_Value,
                MaxSendInfo_ValueCom);
        fPara.MaxSendWindow_Value = Math.min(fPara.MaxSendWindow_Value,
                MaxRecWindow_ValueCom);
        fPara.MaxRecWindow_Value = Math.min(fPara.MaxRecWindow_Value,
                MaxSendWindow_ValueCom);
    }

    /**
     * 解析波特率
     *
     * @param baudRates 波特率特征字
     * @return
     */
    private int strToIntBaud(String baudRates) {
        int BdRate = 300;
        char[] caBaudrates = baudRates.toCharArray();
        char baud = caBaudrates[0];
        int tmpInt = (int) ((char) (int) baud & 0x7f);
        int iBaudRates = tmpInt - 48;
        switch (iBaudRates) {
            case 0:
                BdRate = 300;
                break;
            case 1:
                BdRate = 600;
                break;
            case 2:
                BdRate = 1200;
                break;
            case 3:
                BdRate = 2400;
                break;
            case 4:
                BdRate = 4800;
                break;
            case 5:
                BdRate = 9600;
                break;
            case 6:
                BdRate = 19200;
                break;
            default:
                break;
        }
        return BdRate;
    }

    /**
     * 协议参数拷贝
     *
     * @param paraDes    HXFramePara
     * @param paraSource HXFramePara
     */
    private void copyPara(HXFramePara paraDes, HXFramePara paraSource) {
        paraDes.aesKey = paraSource.aesKey;
        paraDes.auKey = paraSource.auKey;
        paraDes.BlockNum = paraSource.BlockNum;
        paraDes.DestAddr = paraSource.DestAddr;
        paraDes.encryptionMethod = paraSource.encryptionMethod;
        paraDes.enKey = paraSource.enKey;
        paraDes.enLevel = paraSource.enLevel;
        paraDes.ErrTxt = paraSource.ErrTxt;
        paraDes.FirstFrame = paraSource.FirstFrame;
        paraDes.frameCnt = paraSource.frameCnt;
        paraDes.FrameShowTxt = paraSource.FrameShowTxt;
        paraDes.MaxRecInfo_Value = paraSource.MaxRecInfo_Value;
        paraDes.MaxRecWindow_Value = paraSource.MaxRecWindow_Value;
        paraDes.MaxSendInfo_Value = paraSource.MaxSendInfo_Value;
        paraDes.MaxSendWindow_Value = paraSource.MaxSendWindow_Value;
        paraDes.MeterNo = paraSource.MeterNo;
        paraDes.Mode = paraSource.Mode;
        paraDes.Nrec = paraSource.Nrec;
        paraDes.Nsend = paraSource.Nsend;
        paraDes.OBISattri = paraSource.OBISattri;
        paraDes.Pwd = paraSource.Pwd;
        paraDes.RecData = paraSource.RecData;
        paraDes.ByteWaitT = paraSource.ByteWaitT;
        paraDes.SourceAddr = paraSource.SourceAddr;
        paraDes.sysTitleC = paraSource.sysTitleC;
        paraDes.sysTitleS = paraSource.sysTitleS;
        paraDes.dataFrameWaitTime = paraSource.dataFrameWaitTime;
        paraDes.WriteData = paraSource.WriteData;
    }

    /**
     * 协议回复异常解析提示
     *
     * @param FaultCode byte
     * @return string
     */
    private String accessResult(byte FaultCode) {
        int FaultStr = (int) (FaultCode);
        String RtnStr = "";
        switch (FaultStr) {
            case 1:
                RtnStr = "DLMS_HARDWARE_FAULT";
                break;
            case 2:
                RtnStr = "DLMS_TEMP_FAILURE";
                break;
            case 3:
                RtnStr = "DLMS_RW_DENIED";
                break;
            case 4:
                RtnStr = "DLMS_OBJECT_UNDEF";
                break;
            case 9:
                RtnStr = "DLMS_OBJECT_INCON";
                break;
            case 11:
                RtnStr = "DLMS_OBJECT_UNAVAILABLE";
                break;
            case 12:
                RtnStr = "DLMS_TYPE_UNMATCHED";
                break;
            case 13:
                RtnStr = "DLMS_ACCESS_VIOLATED";
                break;
            case 14:
                RtnStr = "DLMS_DATA_UNAVAILABLE";
                break;
            case 15:
                RtnStr = "DLMS_LONGGET_ABORTED";
                break;
            case 16:
                RtnStr = "DLMS_NOLONG_GET";
                break;
            case 17:
                RtnStr = "DLMS_LONGSET_ABORTED";
                break;
            case 18:
                RtnStr = "DLMS_NOLONG_SET";
                break;
            default:
                RtnStr = "DLMS_DATA_DENIED";
                break;
        }
        return RtnStr;
    }

    /**
     * 协议参数Get方法
     *
     * @return HXFramePara
     */
    public HXFramePara getCommParaModel() {
        return commParaModel;
    }

    /**
     * 协议参数设置方法
     *
     * @param commParaModel HXFramePara
     */
    public void setCommParaModel(HXFramePara commParaModel) {
        this.commParaModel = commParaModel;
    }
}
