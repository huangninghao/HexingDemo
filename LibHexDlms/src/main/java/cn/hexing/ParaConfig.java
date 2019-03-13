package cn.hexing;

import cn.hexing.dlms.HexClientAPI;
import cn.hexing.dlt645.HexClient645API;
import cn.hexing.iec21.HexClient21API;
import cn.hexing.iec21.iprotocol.HexHandLC;
import cn.hexing.model.HXFramePara;

/**
 * @author caibinglong
 *         date 2018/6/13.
 *         desc desc
 */

public class ParaConfig {
    /**
     * 波特率
     */
    public int baudRate;

    /**
     * rf 通道 波特率
     */
    public int channelBaudRate;

    /**
     * 数据位
     */
    public int nBits;
    /**
     * 停止位
     */
    public int nStop;
    /**
     * 校验
     */
    public String sVerify;
    /**
     * 握手超时时间
     */
    public long handWaitTime;
    /**
     * 数据读取超时时间
     */
    public long dataFrameWaitTime;
    /**
     * 是否第一帧
     */
    public boolean firstFrame;
    /**
     * 是否握手
     */
    public boolean isHands;

    public int handType = -1;

    /**
     * 是否固定通道
     */
    public int fixedChannel = -1;//固定通道0  1,2,3,4,5,6

    /**
     * 加密级别
     */
    public byte enLevel;
    public HXFramePara.AuthMode authMode;
    public HXFramePara.AuthMethod encryptionMode;
    /**
     * 表号
     */
    public String strMeterNo;
    /**
     * 表密码
     */
    public String strMeterPwd;
    /**
     * 设置串口地址
     */
    public String strComName;

    /**
     * 设备类型
     */
    public String deviceType;

    /**
     * 通讯方式
     * <p>
     * 光电头 串口METHOD_OPTICAL = 1;
     * <p>
     * 蓝牙 METHOD_BLUETOOTH = 2;
     * <p>
     * 射频 串口发送 METHOD_RF = 3;
     */

    public int commMethod;
    /**
     * 停顿时间发下一数据帧
     */
    public long sleepSendTime;

    //8位数据位 是否转换 7位数据位发送
    public boolean isBitConversion = false;
    public boolean isBaudRateTest = false;
    public long changeBaudRateSleepTime = 300; //ms 切换波特率等待时间
    //收到数据 是否 & 7F 计算 适用于 数据位7 偶校验
    //收到数据有偏差需要每个byte & 0x7F 得到正确数据
    public boolean recDataConversion = false;

    public boolean debugMode;

    public ParaConfig() {
        this.baudRate = 300;
        this.nBits = 8;
        this.nStop = 1;
        this.sVerify = "N";
        this.handWaitTime = 1200;
        this.dataFrameWaitTime = 1500;
        this.firstFrame = true;
        this.isHands = true;
        this.enLevel = 0X00;
        this.authMode = HXFramePara.AuthMode.HLS;
        this.encryptionMode = HXFramePara.AuthMethod.AES_GCM_128;
        this.strMeterNo = "254455455";
        this.deviceType = HexDevice.KT50;
        this.sleepSendTime = 5;
        this.debugMode = false;
        this.strMeterPwd = "000000000000";
        this.isBitConversion = false;
        this.recDataConversion = false;
        this.isBaudRateTest = false;
        this.changeBaudRateSleepTime = 300;
        this.channelBaudRate = 4800;
        this.fixedChannel = -1;
        this.handType = HexHandType.HEXING;
    }

    public static class Builder {
        ParaConfig config = new ParaConfig();

        /**
         * 设置加密
         *
         * @param enLevel 0x00 不加密  目前加密是0X03
         */
        public Builder setEnLevel(byte enLevel) {
            this.config.enLevel = enLevel;
            return this;
        }

        public Builder setHandWaitTime(long waitTime) {
            this.config.handWaitTime = waitTime;
            return this;
        }

        public Builder setDataFrameWaitTime(long waitTime) {
            this.config.dataFrameWaitTime = waitTime;
            return this;
        }

        /**
         * 设置校验
         *
         * @param verify Y或N
         */
        public Builder setVerify(String verify) {
            this.config.sVerify = verify;
            return this;
        }

        /**
         * 设置方式
         *
         * @param authMode 授权方式
         */
        public Builder setAuthMode(HXFramePara.AuthMode authMode) {
            this.config.authMode = authMode;
            return this;
        }

        /**
         * 设置方式
         *
         * @param encryptionMode 加密方式
         */
        public Builder setAuthMethod(HXFramePara.AuthMethod encryptionMode) {
            this.config.encryptionMode = encryptionMode;
            return this;
        }

        /**
         * 设置 通讯设备 及通讯方式
         *
         * @param deviceType 设备类型
         */
        public Builder setDevice(String deviceType) {
            this.config.deviceType = deviceType;
            return this;
        }

        /**
         * 设置通讯方式
         *
         * @param commMethod 通讯方式
         */
        public Builder setCommMethod(int commMethod) {
            this.config.commMethod = commMethod;
            return this;
        }

        /**
         * 设置串口地址
         *
         * @param comName ru "/dev/ttyUSB0"
         * @return Builder
         */
        public Builder setComName(String comName) {
            this.config.strComName = comName;
            return this;
        }

        public Builder setIsFirstFrame(boolean bool) {
            this.config.firstFrame = bool;
            return this;
        }

        public Builder setIsHands(boolean bool) {
            this.config.isHands = bool;
            return this;
        }

        /**
         * 设置波特率
         *
         * @param baudRate int
         */
        public Builder setBaudRate(int baudRate) {
            this.config.baudRate = baudRate;
            return this;
        }

        public Builder setIsBaudRate(boolean bool) {
            this.config.isBaudRateTest = bool;
            return this;
        }

        public Builder setStrMeterNo(String meterNo) {
            this.config.strMeterNo = meterNo;
            return this;
        }

        public Builder setStrMeterPwd(String meterPwd) {
            this.config.strMeterPwd = meterPwd;
            return this;
        }

        /**
         * 设置 数据位
         *
         * @param nBits int
         */
        public Builder setDataBit(int nBits) {
            this.config.nBits = nBits;
            return this;
        }

        /**
         * 设置 停止位
         *
         * @param nStop int
         */
        public Builder setStopBit(int nStop) {
            this.config.nStop = nStop;
            return this;
        }

        public Builder setSleepSendTime(long time) {
            this.config.sleepSendTime = time;
            return this;
        }

        public Builder setSleepChangeBaudRate(long time) {
            this.config.changeBaudRateSleepTime = time;
            return this;
        }

        public Builder setIsBitConversion(boolean isBitConversion) {
            this.config.isBitConversion = isBitConversion;
            return this;
        }

        public Builder setRecDataConversion(boolean recDataConversion) {
            this.config.recDataConversion = recDataConversion;
            return this;
        }

        public Builder setDebugMode(boolean debugMode) {
            this.config.debugMode = debugMode;
            return this;
        }

        public Builder setChannelBaudRate(int baudRate) {
            this.config.channelBaudRate = baudRate;
            return this;
        }

        public Builder setIsFixedChannel(int channel) {
            this.config.fixedChannel = channel;
            return this;
        }

        public Builder setHandType(@HexHandType.HandTypes int type) {
            this.config.handType = type;
            return this;
        }

        public HexClientAPI build() {
            return HexClientAPI.getInstance(this.config);
        }

        public HexClient21API build21() {
            return HexClient21API.getInstance(this.config);
        }

        public HexClient645API build645() {
            return HexClient645API.getInstance(this.config);
        }
    }
}
