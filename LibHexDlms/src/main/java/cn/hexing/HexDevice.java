package cn.hexing;

/**
 * @author caibinglong
 *         date 2018/2/1.
 *         desc desc
 */

public class HexDevice {
    public final static String KT50 = "KT50";
    public final static String HT380A = "HT380A";

    /**
     * usb 通讯  一般如KT50 掌机 串口
     */
    public final static String COMM_NAME_USB = "/dev/ttyUSB0";

    /**
     * KT50 RF 通讯 串口
     */
    public final static String COMM_NAME_RF2 = "/dev/ttyMT2";
    public final static String COMM_NAME_RF0 = "/dev/ttyMT0";
    public final static String COMM_NAME_RF1 = "/dev/ttyMT1";
    public final static String COMM_NAME_RF3 = "/dev/ttyMT3";
    public final static String COMM_NAME_RF10 = "/dev/ttyMT10";

    /**
     * KT50 zigbee 通讯串口
     */
    public final static String COMM_NAME_ZIGBEE = "/dev/ttyMT2";

    /**
     * SAC通讯   一般如H380A掌机 串口
     */
    public final static String COMM_NAME_SAC = "/dev/ttySAC3";

    /**
     * 光电头 串口
     */
    public final static int METHOD_OPTICAL = 1;
    /**
     * 蓝牙
     */
    public final static int METHOD_BLUETOOTH = 2;

    /**
     * 射频 串口发送
     */
    public final static int METHOD_RF = 3;

    /**
     * zigbee 通讯模式
     */
    public final static int METHOD_ZIGBEE = 4;

    /**
     * mBus 通讯模式
     */
    public final static int METHOD_MBUS = 5;

    public final static String RF = "RF";
    public final static String OPTICAL = "Optical";
    public final static String ZIGBEE = "Zigbee";
    public final static String MBUS = "MBus";
}
