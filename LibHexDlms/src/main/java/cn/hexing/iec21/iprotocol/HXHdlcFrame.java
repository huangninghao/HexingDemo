package cn.hexing.iec21.iprotocol;


import android.util.Log;

import cn.hexing.HexStringUtil;
import cn.hexing.model.HXFramePara;

/**
 * @author cbl
 * @version 2.0
 *          21S协议帧
 * @Description: 协议的帧函数
 * @Copyright: Copyright (c) 2018
 * @Company 杭州海兴电力科技
 */
public class HXHdlcFrame {

    public String checkout(String str) {
        int res = 0;
        for (int i = 0; i < str.length(); i += 2) {
            res = res ^ Integer.valueOf(str.substring(i, i + 2), 16);
        }
        return String.format("%02x", res & 0xff);
    }

    /**
     * 断开帧
     *
     * @return String 16进制 字符串
     */
    public String getEndFrame(HXFramePara para) {
        StringBuilder sendData = new StringBuilder();
        sendData.append("7F");
        sendData.append("7F");
        sendData.append("7F");
        sendData.append("01");
        sendData.append("42");//B
        sendData.append("30");//0
        sendData.append("03");
        sendData.append(checkout(sendData.substring(8)));
        if (para.isBitConversion) {
            return HexStringUtil.getDisplacement(sendData.toString().toUpperCase());
        }
        return sendData.toString().toUpperCase();
    }

    /**
     * 获取波特率帧
     *
     * @param para HXFramePara
     * @return 数据帧
     */
    public String getBaudRateFrame(HXFramePara para) {
        StringBuilder sendData = new StringBuilder();
        sendData.append("7F");
        sendData.append("7F");
        sendData.append("7F");
        sendData.append("2F");
        sendData.append("3F");
        sendData.append(HexStringUtil.parseAscii(para.getStrMeterNo()));
        sendData.append("21");
        sendData.append("0D");
        sendData.append("0A");
        Log.v("Hdlc波特率", sendData.toString());
        if (para.isBitConversion) {
            return HexStringUtil.getDisplacement(sendData.toString().toUpperCase());
        }
        return sendData.toString().toUpperCase();
    }

    /**
     * 读出模式
     *
     * @param para HXFramePara
     * @return
     */
    public String getReadIECOut(HXFramePara para) {
        StringBuilder sendData = new StringBuilder();
        sendData.append("7F");
        sendData.append("7F");
        sendData.append("7F");
        sendData.append("2F");
        sendData.append("3F");
        sendData.append(HexStringUtil.parseAscii(para.getStrMeterNo()));
        sendData.append("21");
        sendData.append("0D");
        sendData.append("0A");
        if (para.isBitConversion) {
            return HexStringUtil.getDisplacement(sendData.toString().toUpperCase());
        }
        return sendData.toString().toUpperCase();
    }

    /**
     * ReadOut同步帧
     *
     * @param para HXFramePara
     * @return 数据帧
     */
    public String getReadOutSynchronizationFrame(HXFramePara para) {
        //同步帧
        StringBuilder sendData = new StringBuilder();
        sendData.append("7F");
        sendData.append("7F");
        sendData.append("7F");
        sendData.append("06");//ACK
        sendData.append("30");//0
        sendData.append(para.getZWord());//Z字
        sendData.append("30");//0读出模式
        sendData.append("0D");
        sendData.append("0A");
        if (para.isBitConversion) {
            return HexStringUtil.getDisplacement(sendData.toString().toUpperCase());
        }
        return sendData.toString().toUpperCase();
    }

    /**
     * 同步帧
     *
     * @param para HXFramePara
     * @return HXFramePara
     */
    public String getSynchronizationFrame(HXFramePara para) {
        //同步帧
        StringBuilder sendData = new StringBuilder();
        sendData.append("7F");
        sendData.append("7F");
        sendData.append("7F");
        sendData.append("06");//ACK
        sendData.append("30");//0
        sendData.append(para.getZWord());//Z字
        sendData.append("31");//1编程模式
        sendData.append("0D");
        sendData.append("0A");
        System.out.println("send=" + sendData.toString().toUpperCase());
        if (para.isBitConversion) {
            return HexStringUtil.getDisplacement(sendData.toString().toUpperCase());
        }
        return sendData.toString().toUpperCase();
    }

    /**
     * 密码帧
     *
     * @param para HXFramePara
     * @return 数据帧
     */
    public String getPwdFrame(HXFramePara para) {
        //发送密码PassWord
        StringBuilder sendData = new StringBuilder();
        sendData.append("7F");
        sendData.append("7F");
        sendData.append("7F");
        sendData.append("01");
        sendData.append("50");//"P";
        sendData.append("32");//2
        sendData.append("02");
        sendData.append("28");//"(";
        sendData.append(HexStringUtil.parseAscii(para.getMeterPWD()));
        sendData.append("29");//")";
        sendData.append("03");
        sendData.append(checkout(sendData.substring(8)));
        System.out.println("send=" + HexStringUtil.hexToCharStr(sendData.toString().toUpperCase()));
        if (para.isBitConversion) {
            return HexStringUtil.getDisplacement(sendData.toString().toUpperCase());
        }
        return sendData.toString().toUpperCase();
    }


    /**
     * 读取数据帧
     *
     * @param para HXFramePara
     * @return 数据帧
     */
    public String getReadR2Frame(HXFramePara para) {
        //读数据命令帧
        StringBuilder sendData = new StringBuilder();
        sendData.append("7F");
        sendData.append("7F");
        sendData.append("7F");
        sendData.append("01");
        //sendData += "R2";
        sendData.append("52");
        sendData.append("32");
        sendData.append("02");
        sendData.append(HexStringUtil.parseAscii(para.OBISattri));
        //2015-07-12 BY ZJC  带时间段读取
        //sendData += "()";
        sendData.append("28");//  (
        sendData.append(HexStringUtil.parseAscii(para.WriteData));
        sendData.append("29");// ")";
        sendData.append("03");
        sendData.append(checkout(sendData.substring(8)));
        if (para.isBitConversion) {
            return HexStringUtil.getDisplacement(sendData.toString().toUpperCase());
        }
        return sendData.toString().toUpperCase();
    }

    /**
     * 读取数据块
     *
     * @param para HXFramePara
     * @return String
     */
    public String getReadRequestBlockFrame(HXFramePara para) {
        StringBuilder sendData = new StringBuilder();
        sendData.append("06");
        if (para.isBitConversion) {
            return HexStringUtil.getDisplacement(sendData.toString().toUpperCase());
        }
        return sendData.toString().toUpperCase();
    }

    /**
     * 读数据命令帧
     *
     * @param para HXFramePara
     * @return 数据帧
     */
    public String getReadR5Frame(HXFramePara para) {
        //读数据命令帧
        StringBuilder sendData = new StringBuilder();
        sendData.append("7F");
        sendData.append("7F");
        sendData.append("7F");
        sendData.append("01");
        sendData.append("52");//R
        sendData.append("35");//5
        sendData.append("02");
        sendData.append(HexStringUtil.parseAscii(para.OBISattri));
        //2015-07-12 BY ZJC  带时间段读取
        //sendData += "()";
        sendData.append("28");//  (
        sendData.append(HexStringUtil.parseAscii(para.WriteData));
        sendData.append("29");// ")";

        sendData.append("03");
        sendData.append(checkout(sendData.substring(8)));
        if (para.isBitConversion) {
            return HexStringUtil.getDisplacement(sendData.toString().toUpperCase());
        }
        return sendData.toString().toUpperCase();
    }

    /**
     * 写数据帧
     *
     * @param para HXFramePara
     * @return 数据帧
     */
    public String getWriteFrame(HXFramePara para) {
        StringBuilder sendData = new StringBuilder();
        sendData.append("7F");
        sendData.append("7F");
        sendData.append("7F");
        sendData.append("01");
        sendData.append("57");//W
        sendData.append("32");//2
        sendData.append("02");
        sendData.append(HexStringUtil.parseAscii(para.OBISattri));
        sendData.append("28");//  (
        sendData.append(HexStringUtil.parseAscii(para.WriteData));
        sendData.append("29");// ")";

        sendData.append("03");
        sendData.append(checkout(sendData.substring(8)));
        if (para.isBitConversion) {
            return HexStringUtil.getDisplacement(sendData.toString().toUpperCase());
        }
        return sendData.toString().toUpperCase();
    }

    /**
     * 执行帧
     *
     * @param para HXFramePara
     * @return 数据帧
     */
    public String getActionFrame(HXFramePara para) {
        //执行命令
        StringBuilder sendData = new StringBuilder();
        sendData.append("7F");
        sendData.append("7F");
        sendData.append("7F");
        sendData.append("01");
        sendData.append("45");//E
        sendData.append("32");//2
        sendData.append("02");
        sendData.append(HexStringUtil.parseAscii(para.OBISattri));
        sendData.append("28");//  (
        sendData.append(HexStringUtil.parseAscii(para.WriteData));
        sendData.append("29");// ")";
        sendData.append("03");
        sendData.append(checkout(sendData.substring(8)));
        if (para.isBitConversion) {
            return HexStringUtil.getDisplacement(sendData.toString().toUpperCase());
        }
        return sendData.toString().toUpperCase();
    }

}
