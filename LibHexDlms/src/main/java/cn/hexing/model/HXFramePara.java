package cn.hexing.model;

import java.util.List;

import cn.hexing.HexHandType;


public class HXFramePara {

    public enum APDUtype {
        glo_initiateRequest, glo_initiateResponse, glo_get_request,
        glo_set_request, glo_events_notification_request, glo_action_request,
        glo_get_response, glo_set_response, glo_action_response,
    }

    public enum AuthMethod {
        AES_GCM_128, MD5,
    }

    public enum AuthMode {
        NONE, LLS, HLS,
    }

    // 表号
    public String MeterNo;

    // 加密方法
    public AuthMethod encryptionMethod;

    // 加密级别
    public byte enLevel;

    // AK
    public byte[] auKey;

    // EK
    public byte[] enKey;

    // 高级身份认证HLSkey
    public byte[] aesKey;

    // 身份认证方式
    public AuthMode Mode;

    // 是否为第一帧
    public boolean FirstFrame;
    /**
     * 是否需要重新握手认证
     */
    public boolean isHands;

    /**
     * 是否固定通道
     */
    public int isFixedChannel = -1;//固定通道0

    /**
     * 波特率
     */
    public int baudRate = -1;

    /**
     * rf 通道 波特率
     */
    public int channelBaudRate = -1;

    //设备类型
    public String CommDeviceType;

    // 参数值(用于读时，带参数)
    public String WriteData;

    // 发送之后停顿多少时间
    public int SleepT = 0;

    //切换波特率之前 等待时间
    public long sleepChangeBaudRate = 300;

    /**
     * 数据帧 接收超时时间
     */
    public int dataFrameWaitTime = 1000;

    /**
     * 默认等待时间
     * 用于握手帧 切换波特率
     */
    public int handWaitTime;

    // 字节超时
    public int ByteWaitT;

    // 错误
    public String ErrTxt;

    //write 或 execute 的返回值  写或执行 带返回值
    public String actionResult;

    // 帧内容
    public String FrameShowTxt;

    // 帧数量
    public int frameCnt;

    public String StrsysTitleC;
    // 厂家信息
    public byte[] sysTitleC;

    // 厂家信息返回
    public byte[] sysTitleS;

    // 分类+OBIS+属性+"00",21协议是则为IDCODE
    public String OBISattri;

    // 分包返回时的包数
    public int BlockNum;

    // 最大发送字节
    public int MaxSendInfo_Value;

    // 最大接收字节
    public int MaxRecInfo_Value;

    // / 最大发送窗体
    public int MaxSendWindow_Value;

    // 最大接收窗体
    public int MaxRecWindow_Value;

    // 最大接收字节数
    public byte[] RecData;

    // 目标地址
    public byte[] DestAddr;

    //表地址
    public String strMeterNo;

    // 源地址
    public byte SourceAddr;

    // 发送计数 SSS
    public int Nsend;

    // 接收计数 RRR
    public int Nrec;

    // 低级身份认证LLSkey 21也使用
    public String Pwd;

    // 21专用
    // z字
    private String ZWord;

    // 表计编程密码
    private String MeterPWD;

    //8位数据位 是否转换 7位数据位发送
    public boolean isBitConversion = false;

    //收到数据 是否 & 7F 计算 适用于 数据位7 偶校验
    //收到数据有偏差需要每个byte & 0x7F 得到正确数据
    public boolean recDataConversion = false;

    public boolean isConBaudRate = true;
    public boolean baudRateTest = false;//握手波特率切换测试
    public int handType = HexHandType.HEXING; //握手类型
    //645 start
    public String c645Address; //采集器地址 645

    public int sendType = -1; //发送类型  645

    public int filterReceiveType = -1; //接收过滤类型  645
    public String collectorNo = ""; //采集器编号  645
    public byte[] zigbeeLongAddress;// 645 协议
    public byte[] zigbeeShortAddress; //645 协议

    //645 end

    public String getWriteDataType() {
        return writeDataType;
    }

    public void setWriteDataType(String writeDataType) {
        this.writeDataType = writeDataType;
    }

    //写入数据类型
    public String writeDataType;


    public String getRecDataType() {
        return recDataType;
    }

    public void setRecDataType(String recDataType) {
        this.recDataType = recDataType;
    }

    //接收数据类型
    private String recDataType;

    /**
     * 数据块对象集合
     */
    private List<TranXADRAssist> listTranXADRAssist;

    public void setStrsysTitleC(String strsysTitleC) {
        StrsysTitleC = strsysTitleC;
    }

    public String getStrMeterNo() {
        return strMeterNo;
    }

    public List<TranXADRAssist> getListTranXADRAssist() {
        return listTranXADRAssist;
    }

    public void setListTranXADRAssist(List<TranXADRAssist> listTranXADRAssist) {
        this.listTranXADRAssist = listTranXADRAssist;
    }

    public int getSleepT() {
        return SleepT;
    }

    public void setSleepT(int sleepT) {
        SleepT = sleepT;
    }

    public int getDataFrameWaitTime() {
        return dataFrameWaitTime;
    }

    public void setDataFrameWaitTime(int dataFrameWaitTime) {
        this.dataFrameWaitTime = dataFrameWaitTime;
    }

    public int getHandWaitTime() {
        return handWaitTime;
    }

    public void setHandWaitTime(int handWaitTime) {
        this.handWaitTime = handWaitTime;
    }

    public String getZWord() {
        return ZWord;
    }

    public void setZWord(String zWord) {
        ZWord = zWord;
    }

    public String getMeterPWD() {
        return MeterPWD;
    }

    public void setMeterPWD(String meterPWD) {
        MeterPWD = meterPWD;
    }
}
