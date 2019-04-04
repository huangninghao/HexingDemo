package cn.hexing.dlms.protocol.dlms;

import android.os.SystemClock;

import cn.hexing.HexDevice;
import cn.hexing.HexHandType;
import cn.hexing.HexStringUtil;
import cn.hexing.iComm.AbsCommAction;
import cn.hexing.iComm.ICommAction;
import cn.hexing.dlms.protocol.iprotocol.IProtocol;
import cn.hexing.dlms.protocol.model.HexActionBean;
import cn.hexing.model.HXFramePara;
import cn.hexing.model.TranXADRAssist;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * @author 王昌豹
 * @version 1.0
 *          DLMS协议类
 *          DLMS协议，读、写、执行电表
 *          Copyright (c) 2016
 *          杭州海兴电力科技
 */
public class HXHdlcDLMS implements IProtocol {

    // 发送计数 SSS
    private int Nsend = 0;
    // 接收计数 RRR
    private int Nrec = 0;
    int frameCnt = 0;
    HXHdlcDLMSFrame hdlcframe = new HXHdlcDLMSFrame();
    // 转换的波特率
    private int ToBaudTate = 300;
    private HXFramePara commParaModel;

    /**
     * DLMS-握手
     *
     * @param paraModel
     * @param commDevice
     * @return
     */
    private boolean Handclasp(HXFramePara paraModel, ICommAction commDevice) {
        try {
            commDevice.setBaudRate(300);
            Nrec = 0;
            Nsend = 0;
            paraModel.Nrec = Nrec;
            paraModel.Nsend = Nsend;
            byte[] sndByt = hdlcframe.getHandclaspFrame();
            boolean isSend = commDevice.sendByt(sndByt);
            // 检查是否发送成功，发送成功
            if (!isSend) {
                paraModel.ErrTxt = "Serial port access denied!";// 返回错误代码，串口打开失败
                return false;
            }
            byte[] receiveByt = commDevice.receiveByt(paraModel.getHandWaitTime(), paraModel.getDataFrameWaitTime());
            if (receiveByt.length > 0) {
                if (receiveByt.length > 5) {
                    // 发送Z字
                    sndByt = hdlcframe.getZFrame(receiveByt[4]);
                    SystemClock.sleep(paraModel.SleepT);
                    isSend = commDevice.sendByt(sndByt);
                    if (!isSend) {
                        // 返回错误代码，串口打开失败
                        paraModel.ErrTxt = "Serial port access denied!";
                        return false;
                    }
                    // 读Z字
                    if (receiveByt[0] == 0x55) {
                        ToBaudTate = strToIntBaud(String
                                .valueOf((char) receiveByt[5]));

                    } else {
                        ToBaudTate = strToIntBaud(String
                                .valueOf((char) receiveByt[4]));
                    }
                    SystemClock.sleep(paraModel.sleepChangeBaudRate);
                    commDevice.setBaudRate(ToBaudTate);
                    commDevice.receiveByt(paraModel.getHandWaitTime(), paraModel.getDataFrameWaitTime());
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
     * -握手 伊拉克表
     *
     * @param paraModel  HXFramePara
     * @param commDevice ICommAction
     * @return boolean
     */
    private boolean Handclasp2(HXFramePara paraModel, ICommAction commDevice) {
        try {
            commDevice.setBaudRate(paraModel.baudRate);
            Nrec = 0;
            Nsend = 0;
            paraModel.Nrec = Nrec;
            paraModel.Nsend = Nsend;
            byte[] sndByt = hdlcframe.getHandclaspFrame();
            boolean isSend = commDevice.sendByt(sndByt);
            // 检查是否发送成功，发送成功
            if (!isSend) {
                paraModel.ErrTxt = "Serial port access denied!";// 返回错误代码，串口打开失败
                return false;
            }
            byte[] receiveByt = commDevice.receiveByt(paraModel.sleepReceiveT, paraModel.getDataFrameWaitTime());
            if (receiveByt != null && receiveByt.length > 1) {
                return true;
            } else {
                paraModel.ErrTxt = "Over time!";
                return false;
            }
        } catch (Exception ex) {
            paraModel.ErrTxt = "Er2:" + ex.getMessage();
            return false;
        }
    }

    /**
     * DLMS-None身份验证
     *
     * @param fpara
     * @param commDevice
     * @return
     */
    private boolean LinkNoAuth(HXFramePara fpara, ICommAction commDevice) {
        byte[] sndByt = null;
        byte[] receiveByt = null;
        boolean isSend = false;
        Nrec = 0;
        Nsend = 0;
        fpara.Nrec = Nrec;
        fpara.Nsend = Nsend;
        sndByt = hdlcframe.getNoAuthSNRMFrame(fpara);
        isSend = commDevice.sendByt(sndByt);
        if (!isSend) {
            // 协议提示通讯错误，通讯日志记录各类错误
            fpara.ErrTxt = "DLMS_SNRM_FAILED";
            return false;
        }
        receiveByt = commDevice.receiveByt(fpara.sleepReceiveT, fpara.dataFrameWaitTime);
        if (receiveByt != null && receiveByt.length > 0) {
            if (!checkFrame(receiveByt, false, fpara)) {
                return false;
            }
            getLinkPara(receiveByt, fpara);
        } else {
            fpara.ErrTxt = "DLMS_OVER_TIME";
            return false;
        }
        sndByt = hdlcframe.getNoAuthAARQFrame(fpara);
        SystemClock.sleep(fpara.SleepT);
        isSend = commDevice.sendByt(sndByt);
        if (!isSend) {
            fpara.ErrTxt = "DLMS_AARQ_FAILED";
            return false;
        }
        Nsend = 1;
        fpara.Nsend = Nsend;
        receiveByt = commDevice.receiveByt(fpara.sleepReceiveT, fpara.dataFrameWaitTime);
        if (receiveByt != null) {
            if (!checkFrame(receiveByt, true, fpara)) {
                fpara.ErrTxt = "DLMS_FORMAT_ERROR";
                return false;
            }
            Nrec = 1;
            fpara.Nrec = 1;
        } else {
            fpara.ErrTxt = "DLMS_OVER_TIME";
            return false;
        }
        return true;
    }

    /**
     * DLMS-LLS身份认证
     *
     * @param fpara
     * @param commDevice
     * @return
     */
    private boolean LinkLLSAuth(HXFramePara fpara, ICommAction commDevice) {
        byte[] sndByt = null;
        byte[] receiveByt = null;
        boolean isSend;
        Nrec = 0;
        Nsend = 0;
        int errNum = 4;
        while (errNum > 0) {
            if (errNum == 2) {
                commDevice.setBaudRate(9600);
                System.out.println("LLS验证，波特率切换9600尝试");
            } else if (errNum == 1) {
                commDevice.setBaudRate(4800);
                System.out.println("LLS验证，波特率切换4800尝试");
            }
            sndByt = hdlcframe.getNoAuthSNRMFrame(fpara);
            isSend = commDevice.sendByt(sndByt);
            if (!isSend) {
                fpara.ErrTxt = "DLMS_SNRM_FAILED";
                return false;
            }
            receiveByt = commDevice.receiveByt(fpara.sleepReceiveT, fpara.dataFrameWaitTime);
            errNum--;
            if (receiveByt.length > 0) {
                break;
            }
        }

        if (receiveByt.length > 0) {
            if (!checkFrame(receiveByt, false, fpara)) {
                return false;
            }
            getLinkPara(receiveByt, fpara);

        } else {
            fpara.ErrTxt = "DLMS_OVER_TIME";
            return false;
        }
        sndByt = hdlcframe.getLLSAuthAARQFrame(fpara);
        SystemClock.sleep(200);
        isSend = commDevice.sendByt(sndByt);
        if (!isSend) {
            fpara.ErrTxt = "DLMS_AARQ_FAILED";
            return false;
        }
        Nsend = 1;
        fpara.Nsend = Nsend;
        receiveByt = commDevice.receiveByt(fpara.sleepReceiveT, fpara.dataFrameWaitTime);
        if (receiveByt != null) {
            if (!checkFrame(receiveByt, true, fpara)) {
                return false;
            }
            Nrec = 1;
            fpara.Nrec = Nrec;

        } else {

            fpara.ErrTxt = "DLMS_OVER_TIME";
            return false;
        }
        return true;
    }

    /**
     * DLMS-HLS身份认证
     *
     * @param fpara
     * @param commDevice
     * @return
     */
    private boolean linkHLSAuth(HXFramePara fpara, ICommAction commDevice) {
        Nrec = 0;
        Nsend = 0;
        byte[] receiveByt = new byte[0];
        boolean isSend;
        int errNum = 2;
        byte[] sndByt;
        while (errNum > 0) {
            if (errNum <= 1) {
                sndByt = HexStringUtil.hexToByte("7E A0 23 00 02 FE FF 03 93 E4 B0 81 80 14 05 02 07 D0 06 02 07 D0 07 04 00 00 00 01 08 04 00 00 00 01 3A F2 7E".replace(" ", ""));
                System.out.println("HLS验证，波特率切换4800尝试");
            } else {
                sndByt = hdlcframe.getNoAuthSNRMFrame(fpara);
                System.out.println("HLS验证");
            }
            isSend = commDevice.sendByt(sndByt);
            if (!isSend) {
                fpara.ErrTxt = "DLMS_SNRM_FAILED";
                return false;
            }
            receiveByt = commDevice.receiveByt(fpara.sleepReceiveT, fpara.dataFrameWaitTime);
            errNum--;
            if (receiveByt.length > 0) {
                break;
            }
        }

        if (receiveByt.length > 0) {
            if (!checkFrame(receiveByt, false, fpara)) {
                return false;
            }
            getLinkPara(receiveByt, fpara);
        } else {
            fpara.ErrTxt = "DLMS_OVER_TIME";
            return false;
        }
        frameCnt = 1;
        fpara.frameCnt = frameCnt;
        sndByt = hdlcframe.getHLSAuthAARQFrame(fpara);
        SystemClock.sleep(200);
        isSend = commDevice.sendByt(sndByt);
        if (!isSend) {
            fpara.ErrTxt = "DLMS_AARQ_FAILED";
            return false;
        }
        Nsend = 1;
        fpara.Nsend = 1;
        receiveByt = commDevice.receiveByt(fpara.sleepReceiveT, fpara.dataFrameWaitTime);
        if (receiveByt != null && receiveByt.length > 0) {
            if (!checkFrame(receiveByt, true, fpara)) {
                return false;
            }
            Nrec = 1;
            fpara.Nrec = Nrec;
            // 提取随机数，对其加密
            fpara.frameCnt = frameCnt;
            HXFramePara paraModelCopy = new HXFramePara();
            copyPara(paraModelCopy, fpara);
            paraModelCopy.OBISattri = "000F0000280000FF01";
            paraModelCopy.FirstFrame = false;
            commParaModel.isHands = false;
            paraModelCopy.WriteData = hdlcframe.getStoc(paraModelCopy, receiveByt);
            if (!action(paraModelCopy, commDevice)) {
                return false;
            }
        } else {

            fpara.ErrTxt = "DLMS_OVER_TIME";
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
    private boolean changeChannel(HXFramePara para, ICommAction commDevice) {
        if (para == null) {
            return false;
        }
        if (para.channelBaudRate > 0) {
            commDevice.setBaudRate(para.channelBaudRate);
        }
        System.out.println("通道切换，波特率=" + para.channelBaudRate);
        boolean isSend = commDevice.sendByt(hdlcframe.getChangeChannel(para));
        if (isSend) {
            byte[] receiveByt = commDevice.receiveByt(para.sleepReceiveT, para.dataFrameWaitTime);
            if (receiveByt != null && receiveByt.length > 0) {
                commDevice.setBaudRate(para.baudRate);
                System.out.println("切换通讯波特率=" + para.baudRate);
                return true;
            }
        }
        return false;
    }

    /**
     * 改变通道
     *
     * @param para       HXFramePara
     * @param commDevice ICommAction
     * @return bool
     */
    private boolean changeChannel(int channelBaudRate, int baudRate,
                                  HXFramePara para, ICommAction commDevice) {
        if (para == null) {
            return false;
        }
        commDevice.setBaudRate(channelBaudRate);
        boolean isSend = commDevice.sendByt(hdlcframe.getChangeChannel(para));
        if (isSend) {
            byte[] receiveByt = commDevice.receiveByt(para.sleepReceiveT, para.dataFrameWaitTime);
            if (receiveByt != null && receiveByt.length > 0) {
                commDevice.setBaudRate(baudRate);
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized byte[] read(HXFramePara paraModel, ICommAction commDevice) {
        commParaModel = paraModel;
        ArrayList<Byte> rtnReceiveByt = new ArrayList<>();
        paraModel.Nsend = Nsend;
        paraModel.Nrec = Nrec;
        if (paraModel.FirstFrame) {
            if (HexDevice.OPTICAL.equals(paraModel.CommDeviceType)) {
                if (!Handclasp(paraModel, commDevice)) {
                    paraModel.ErrTxt = "DLMS_AUTH_FAILED";
                    return null;
                }
            } else if (HexDevice.RF.equals(paraModel.CommDeviceType)) {
                if (paraModel.handType == HexHandType.IRAQ) {
                    //伊拉克表
                    if (!Handclasp2(paraModel, commDevice)) {
                        paraModel.ErrTxt = "DLMS_AUTH_FAILED";
                        return null;
                    }
                } else {
                    if (!changeChannel(paraModel, commDevice)) {
                        paraModel.ErrTxt = "DLMS_CHANNEL_FAILED";
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

        if (Nsend == 0 && paraModel.CommDeviceType.equals(HexDevice.MBUS)) {
            //mbus 特殊处理
            Nsend = 3;
            Nrec = 1;
        }
        frameCnt++;
        paraModel.frameCnt = frameCnt;
        paraModel.Nrec = Nrec;
        paraModel.Nsend = Nsend;
        byte[] sndByt = hdlcframe.getReadRequestNormalFrame(paraModel);
        if (!paraModel.FirstFrame) {
            SystemClock.sleep(paraModel.SleepT);
        }
        boolean isSend = commDevice.sendByt(sndByt);
        if (!isSend) {
            paraModel.ErrTxt = "DLMS_NORMAL_FAILED";
            return new byte[0];
        }
        Nsend++;
        paraModel.Nsend = Nsend;
        byte[] receiveByt = commDevice.receiveByt(paraModel.sleepReceiveT, paraModel.dataFrameWaitTime);
        if (receiveByt == null || receiveByt.length == 0 || !checkFrame(receiveByt)) {
            return new byte[0];
        }
        if (paraModel.enLevel != 0x00)// 如果有加密或认证，则将收到数据还原
        {
            receiveByt = hdlcframe.getOriginalData(receiveByt, paraModel);
        }
        if (receiveByt != null && (receiveByt[10 + paraModel.DestAddr.length] & 0xff) == 0xd8) {
            paraModel.ErrTxt = "DLMS_SER_NOTALLOWED";
            return new byte[0];
        }
        if (receiveByt != null && (receiveByt[13 + paraModel.DestAddr.length] & 0xff) != 0x00) {
            paraModel.ErrTxt = accessResult(receiveByt[14 + paraModel.DestAddr.length]);
            return new byte[0];
        }
        Nrec++;
        paraModel.Nrec = Nrec;
        if (receiveByt != null && (receiveByt[11 + paraModel.DestAddr.length] & 0xff) == 0x01) {
            for (int i = 14 + paraModel.DestAddr.length; i < receiveByt.length - 3; i++) {
                rtnReceiveByt.add(receiveByt[i]);
            }
        }
        if (receiveByt != null && (receiveByt[11 + paraModel.DestAddr.length] & 0xff) == 0x02) {
            boolean IsLastFrame = false;
            int BlockNum = 1;
            paraModel.BlockNum = BlockNum;
            int addLen = paraModel.DestAddr.length;
            if ((receiveByt[19 + addLen] & 0xff) >= 0x80) {
                //2个字节 代表 数据内容长度
                for (int i = 21 + addLen; i < receiveByt.length - 3; i++) {
                    rtnReceiveByt.add(receiveByt[i]);
                }
            } else {
                for (int i = 20 + addLen; i < receiveByt.length - 3; i++) {
                    rtnReceiveByt.add(receiveByt[i]);
                }
            }
            while (!IsLastFrame) {
                frameCnt++;
                paraModel.frameCnt = frameCnt;
                paraModel.Nsend = Nsend;
                paraModel.Nrec = Nrec;
                sndByt = hdlcframe.getReadRequestBlockFrame(paraModel);
                SystemClock.sleep(paraModel.SleepT);
                isSend = commDevice.sendByt(sndByt);
                if (!isSend) {
                    paraModel.ErrTxt = "DLMS_BLOCK_FAILED";
                    return null;
                }
                Nsend++;
                paraModel.Nsend = Nsend;
                receiveByt = commDevice.receiveByt(paraModel.sleepReceiveT, paraModel.dataFrameWaitTime);
                if (receiveByt != null) {
                    // 接受校验CRC
                    if (!checkFrame(receiveByt)) {
                        paraModel.ErrTxt = "DLMS_FRAME_ERROR";
                        return null;
                    }
                    Nrec++;
                    paraModel.Nsend = Nrec;

                    if (paraModel.enLevel != 0x00)// 如果有加密或认证，则将收到数据还原
                    {
                        receiveByt = hdlcframe.getOriginalData(receiveByt, paraModel);
                    }
                    int pos = 20;
                    if (receiveByt.length > 19 + addLen) {
                        int len = receiveByt[19 + addLen] & 0xff;
                        if (len >= 0X80) {
                            //2个字节 代表 数据内容长度
                            //例如 82 01 34 两个字节代表长度
                            //例如 81 86 只有一个字节代表长度
                            //2个字节 代表 数据内容长度
                            String hex = HexStringUtil.toHex(len);
                            hex = hex.substring(1, hex.length());
                            pos = 20 + Integer.parseInt(hex);
                        }
                    }

                    for (int i = pos + addLen; i < receiveByt.length - 3; i++) {
                        rtnReceiveByt.add(receiveByt[i]);
                    }

                    IsLastFrame = !(receiveByt[13 + addLen] == 0x00);
                    BlockNum = (int) (receiveByt[14 + addLen]
                            * 0x100
                            * 0x100
                            * 0x100
                            + receiveByt[15 + addLen]
                            * 0x100
                            * 0x100
                            + receiveByt[16 + addLen]
                            * 0x100 + receiveByt[17 + addLen]);
                    paraModel.BlockNum = BlockNum;
                } else {
                    paraModel.ErrTxt = "DLMS_OVER_TIME";
                    return null;
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
    public synchronized boolean write(HXFramePara paraModel, ICommAction commDevice) {

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
            sndByt = hdlcframe.getWriteRequestNormalFrame(paraModel);

            if (!paraModel.FirstFrame) {
                SystemClock.sleep(paraModel.SleepT);
            }
            isSend = commDevice.sendByt(sndByt);
            if (!isSend) {
                paraModel.ErrTxt = "DLMS_WRITE_FAILED";
                return false;
            }
            Nsend++;
            paraModel.Nsend = Nsend;
            receiveByt = commDevice.receiveByt(paraModel.sleepReceiveT, paraModel.dataFrameWaitTime);
            if (receiveByt.length == 0 || !checkFrame(receiveByt)) {
                return false;
            }
            // 如果有加密或认证，则将收到数据还原
            if (paraModel.enLevel != 0x00) {
                receiveByt = hdlcframe.getOriginalData(receiveByt, paraModel);
            }
            if (receiveByt.length > (10 + paraModel.DestAddr.length) && receiveByt[10 + paraModel.DestAddr.length] == 0xd8) {
                paraModel.ErrTxt = "DLMS_SER_NOTALLOWED";
                return false;
            }
            if (receiveByt.length >= (13 + paraModel.DestAddr.length) && receiveByt[13 + paraModel.DestAddr.length] != 0x00) {
                paraModel.ErrTxt = accessResult(receiveByt[14 + paraModel.DestAddr.length]);
                return false;
            } else {
                //Token 设置有返回值，暂时这样处理，后面需要修改Write 函数
                if (receiveByt.length > 18) {
                    paraModel.actionResult = String.format("%02x", receiveByt[18]);
                }
            }
            Nrec++;
            paraModel.Nrec = Nrec;
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
                sndByt = hdlcframe.getWriteRequestBlockFrame(paraModel);
                isSend = commDevice.sendByt(sndByt);
                if (!isSend) {
                    paraModel.ErrTxt = "DLMS_BLOCK_FAILED";
                    return false;
                }
                Nsend++;
                paraModel.Nsend = Nsend;
                BlockNum++;
                receiveByt = commDevice.receiveByt(paraModel.sleepReceiveT,
                        paraModel.dataFrameWaitTime);

                if (receiveByt != null) {
                    Nrec++;
                    paraModel.Nrec = Nrec;
                } else {
                    paraModel.ErrTxt = "DLMS_OVER_TIME";
                    return false;
                }
                // 如果有加密或认证，则将收到数据还原
                if (paraModel.enLevel != 0x00) {
                    receiveByt = hdlcframe.getOriginalData(receiveByt,
                            paraModel);

                }
                if (receiveByt[10 + paraModel.DestAddr.length] == 0xd8) {
                    paraModel.ErrTxt = "DLMS_SER_NOTALLOWED";
                    return false;
                }
                if (receiveByt[13 + paraModel.DestAddr.length] != 0x00) {
                    paraModel.ErrTxt = accessResult(receiveByt[14 + paraModel.DestAddr.length]);
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    public synchronized HexActionBean actionWrite(HXFramePara paraModel, ICommAction commDevice) {
        HexActionBean actionBean = new HexActionBean();
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
                    actionBean.errorMsg = "DLMS_AUTH_FAILED";
                    actionBean.result = false;
                }
            } else if (HexDevice.RF.equals(paraModel.CommDeviceType)) {
                if (!changeChannel(paraModel, commDevice)) {
                    actionBean.errorMsg = "DLMS_CHANNEL_FAILED";
                    actionBean.result = false;
                }
            }

        }
        if (paraModel.isHands) {
            if (paraModel.Mode == HXFramePara.AuthMode.NONE) {
                if (!LinkNoAuth(paraModel, commDevice)) {
                    actionBean.errorMsg = "DLMS_AUTH_FAILED";
                    actionBean.result = false;
                }
            }
            if (paraModel.Mode == HXFramePara.AuthMode.LLS) {
                if (!LinkLLSAuth(paraModel, commDevice)) {
                    actionBean.errorMsg = "DLMS_AUTH_FAILED";
                    actionBean.result = false;
                }
            } else if (paraModel.Mode == HXFramePara.AuthMode.HLS) {
                if (!linkHLSAuth(paraModel, commDevice)) {
                    actionBean.errorMsg = "DLMS_AUTH_FAILED";
                    actionBean.result = false;
                }
            }
        }
        if ((paraModel.WriteData.length() / 2 + 18) < paraModel.MaxSendInfo_Value) {
            frameCnt++;
            paraModel.frameCnt = frameCnt;
            paraModel.Nsend = Nsend;
            paraModel.Nrec = Nrec;
            sndByt = hdlcframe.getWriteRequestNormalFrame(paraModel);
            if (HexDevice.RF.equals(paraModel.CommDeviceType)
                    || !paraModel.FirstFrame) {
                SystemClock.sleep(20);
            }
            isSend = commDevice.sendByt(sndByt);
            if (!isSend) {
                actionBean.errorMsg = "DLMS_WRITE_FAILED";
                actionBean.result = false;
            }
            Nsend++;
            paraModel.Nsend = Nsend;
            receiveByt = commDevice.receiveByt(paraModel.sleepReceiveT, paraModel.dataFrameWaitTime);
            if (receiveByt == null || receiveByt.length == 0
                    || !checkFrame(receiveByt)) {
            }
            // 如果有加密或认证，则将收到数据还原
            if (paraModel.enLevel != 0x00) {
                receiveByt = hdlcframe.getOriginalData(receiveByt, paraModel);
            }
            if (receiveByt != null && receiveByt[10 + paraModel.DestAddr.length] == 0xd8) {
                actionBean.errorMsg = "DLMS_SER_NOTALLOWED";
                actionBean.result = false;
            }
            if (receiveByt != null && receiveByt[13 + paraModel.DestAddr.length] != 0x00) {
                actionBean.errorMsg = accessResult(receiveByt[14 + paraModel.DestAddr.length]);
                actionBean.result = false;
            } else {
                //Token 设置有返回值，暂时这样处理，后面需要修改Write 函数
                if (receiveByt != null && receiveByt.length > 18) {
                    actionBean.bytes = Arrays.copyOfRange(receiveByt, 17, receiveByt.length);
                    actionBean.dataSource = String.format("%02x", receiveByt[18]);
                }
            }
            Nrec++;
            paraModel.Nrec = Nrec;
            actionBean.result = true;
        } else {
            // #region set.request_block
            int BlockNum = 1;
            while (paraModel.WriteData.length() > 0) {
                frameCnt++;
                paraModel.frameCnt = frameCnt;
                paraModel.BlockNum = BlockNum;
                paraModel.Nrec = Nrec;
                paraModel.Nsend = Nsend;
                sndByt = hdlcframe.getWriteRequestBlockFrame(paraModel);
                isSend = commDevice.sendByt(sndByt);
                if (!isSend) {
                    actionBean.errorMsg = "DLMS_BLOCK_FAILED";
                    actionBean.result = false;
                }
                Nsend++;
                paraModel.Nsend = Nsend;
                if (BlockNum == 0xff) {
                }
                BlockNum++;
                receiveByt = commDevice.receiveByt(paraModel.sleepReceiveT,
                        paraModel.dataFrameWaitTime);

                if (receiveByt != null) {
                    Nrec++;
                    paraModel.Nrec = Nrec;
                } else {
                    actionBean.errorMsg = "DLMS_OVER_TIME";
                    actionBean.result = false;
                }
                // 如果有加密或认证，则将收到数据还原
                if (paraModel.enLevel != 0x00) {
                    receiveByt = hdlcframe.getOriginalData(receiveByt,
                            paraModel);

                }
                if (receiveByt[10 + paraModel.DestAddr.length] == 0xd8) {
                    actionBean.errorMsg = "DLMS_SER_NOTALLOWED";
                    actionBean.result = false;
                }
                if (receiveByt[13 + paraModel.DestAddr.length] != 0x00) {
                    actionBean.errorMsg = accessResult(receiveByt[14 + paraModel.DestAddr.length]);
                    actionBean.result = false;
                }
            }
        }
        return actionBean;
    }

    @Override
    public boolean action(HXFramePara paraModel, ICommAction commDevice) {
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
            } else if (paraModel.Mode == HXFramePara.AuthMode.HLS) {
                if (!linkHLSAuth(paraModel, commDevice)) {
                    paraModel.ErrTxt = "DLMS_AUTH_FAILED";
                    return false;
                }
            }
        }
        frameCnt++;
        paraModel.frameCnt = frameCnt;
        paraModel.Nsend = Nsend;
        paraModel.Nrec = Nrec;
        sndByt = hdlcframe.getActionRequestNormalFrame(paraModel);
        if (!paraModel.FirstFrame) {
            SystemClock.sleep(paraModel.SleepT);
        }
        isSend = commDevice.sendByt(sndByt);
        if (!isSend) {
            paraModel.ErrTxt = "DLMS_ACTION_FAILED";
            return false;
        }
        Nsend++;
        paraModel.Nsend = Nsend;
        receiveByt = commDevice.receiveByt(paraModel.sleepReceiveT, paraModel.dataFrameWaitTime);
        if (receiveByt != null && receiveByt.length > 0) {
            Nrec++;
            paraModel.Nrec = Nrec;
        } else {
            paraModel.ErrTxt = "DLMS_OVER_TIME";
            return false;
        }
        if (paraModel.enLevel != 0x00)// 如果有加密或认证，则将收到数据还原
        {
            receiveByt = hdlcframe.getOriginalData(receiveByt, paraModel);
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

    @Override
    public byte[] actionAndRead(HXFramePara paraModel, ICommAction commDevice) {
        commParaModel = paraModel;//
        paraModel.Nsend = Nsend;
        paraModel.Nrec = Nrec;
        if (paraModel.FirstFrame) {
            if (HexDevice.OPTICAL.equals(paraModel.CommDeviceType)) {
                if (!Handclasp(paraModel, commDevice)) {
                    paraModel.ErrTxt = "DLMS_AUTH_FAILED";
                    return new byte[0];
                }
            } else if (HexDevice.RF.equals(paraModel.CommDeviceType)) {
                if (!changeChannel(paraModel, commDevice)) {
                    paraModel.ErrTxt = "DLMS_AUTH_FAILED";
                    return new byte[0];
                }
            }

        }
        if (paraModel.isHands) {
            if (paraModel.Mode == HXFramePara.AuthMode.NONE) {
                if (!LinkNoAuth(paraModel, commDevice)) {
                    paraModel.ErrTxt = "DLMS_AUTH_FAILED";
                    return new byte[0];
                }
            } else if (paraModel.Mode == HXFramePara.AuthMode.HLS) {
                if (!linkHLSAuth(paraModel, commDevice)) {
                    paraModel.ErrTxt = "DLMS_AUTH_FAILED";
                    return new byte[0];
                }
            }
        }
        frameCnt++;
        paraModel.frameCnt = frameCnt;
        paraModel.Nrec = Nrec;
        paraModel.Nsend = Nsend;
        ArrayList<Byte> rtnReceiveByt = new ArrayList<>();
        byte[] sendByte = hdlcframe.getReadRequestNormalFrame(paraModel);
        if (!paraModel.FirstFrame) {
            SystemClock.sleep(paraModel.SleepT);
        }
        boolean isSend = commDevice.sendByt(sendByte);
        if (!isSend) {
            paraModel.ErrTxt = "DLMS_NORMAL_FAILED";
            return new byte[0];
        }
        Nsend++;
        paraModel.Nsend = Nsend;
        byte[] receiveByt = commDevice.receiveByt(paraModel.sleepReceiveT, paraModel.dataFrameWaitTime);
        if (receiveByt == null || receiveByt.length == 0 || !checkFrame(receiveByt)) {
            return new byte[0];
        }
        if (paraModel.enLevel != 0x00)// 如果有加密或认证，则将收到数据还原
        {
            receiveByt = hdlcframe.getOriginalData(receiveByt, paraModel);
        }
        if (receiveByt != null && (receiveByt[10 + paraModel.DestAddr.length] & 0xff) == 0xd8) {
            paraModel.ErrTxt = "DLMS_SER_NOTALLOWED";
            return new byte[0];
        }
        if (receiveByt != null && (receiveByt[13 + paraModel.DestAddr.length] & 0xff) != 0x00) {
            paraModel.ErrTxt = accessResult(receiveByt[14 + paraModel.DestAddr.length]);
            return new byte[0];
        }
        Nrec++;
        paraModel.Nrec = Nrec;
        if (receiveByt != null && (receiveByt[11 + paraModel.DestAddr.length] & 0xff) == 0x01) {
            for (int i = 14 + paraModel.DestAddr.length; i < receiveByt.length - 3; i++) {
                rtnReceiveByt.add(receiveByt[i]);
            }
        }

        byte[] bytResult = new byte[rtnReceiveByt.size()];
        for (int i = 0; i < bytResult.length; i++) {
            bytResult[i] = rtnReceiveByt.get(i);
        }
        return bytResult;
    }


    /**
     * 直接发送 byte[] 数据  一般用于固件升级
     *
     * @param paraModel  HXFramePara
     * @param commDevice ICommAction
     * @param assist     TranXADRAssist
     * @return TranXADRAssist
     */
    @Override
    public TranXADRAssist sendByte(HXFramePara paraModel, ICommAction commDevice, TranXADRAssist assist) {
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
        if (paraModel.isBitConversion) {
            assist.processWriteData = HexStringUtil.getDisplacement(assist.writeData);
        } else {
            assist.processWriteData = assist.writeData;
        }

        if (assist.isFirstFrame) {
            discFrame(commDevice);
            commDevice.setBaudRate(9600, 'E', 7, 1);
        }

        boolean isSend = commDevice.sendByt(HexStringUtil.hexToByte(assist.processWriteData));
        if (!isSend) {
            assist.errMsg = "DLMS_SEND_FAILED";
            return assist;
        }
        if (assist.byteLen > 0) {
            assist.recBytes = commDevice.receiveBytToCallback(paraModel.dataFrameWaitTime, assist.byteLen);
        } else {
            assist.recBytes = commDevice.receiveByt(paraModel.sleepReceiveT, paraModel.getDataFrameWaitTime());
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
     * 直接发送 byte[] 数据  一般用于固件升级
     *
     * @param paraModel  HXFramePara
     * @param commDevice ICommAction
     * @param assist     TranXADRAssist
     * @return TranXADRAssist
     */
    public TranXADRAssist sendUpgrade(HXFramePara paraModel, ICommAction commDevice, TranXADRAssist assist) {

        boolean isSend = commDevice.sendByt(HexStringUtil.hexToByte(assist.processWriteData));
        if (!isSend) {
            assist.aResult = false;
            assist.errMsg = "DLMS_SEND_FAILED";
            return assist;
        }

        assist.recBytes = commDevice.receiveByt(paraModel.sleepReceiveT, paraModel.dataFrameWaitTime, assist.byteLen);

        if (assist.recBytes == null || assist.recBytes.length == 0) {
            assist.aResult = false;
        } else {
            assist.recStrData = HexStringUtil.bytesToHexString(assist.recBytes);
            assist.aResult = true;
        }
        return assist;
    }

    @Override
    public boolean discFrame(ICommAction commDevice) {
        if (commParaModel == null || commParaModel.DestAddr == null) {
            return false;
        }
        byte[] TmpArr = hdlcframe.getDISCFrame(commParaModel);
        boolean isSuccess = commDevice.sendByt(TmpArr);
        if (isSuccess) {
            byte[] recData = commDevice.receiveByt(300, 1500);
            if (recData == null) {
                return false;
            }
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
                byte[] FrameEnd = hdlcframe.CRC16(receiveByt, 1,
                        receiveByt.length - 4);
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
        byte[] HCSarr = hdlcframe.CRC16(CheckArr, 1, CheckArr.length - 4);
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
        paraDes.SleepT = paraSource.SleepT;
        paraDes.sleepReceiveT = paraSource.sleepReceiveT;
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
