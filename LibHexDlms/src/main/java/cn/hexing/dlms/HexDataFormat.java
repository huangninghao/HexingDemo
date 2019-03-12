package cn.hexing.dlms;


/**
 * @author caibinglong
 *         date 2018/4/20.
 *         desc dlms 数据类型
 */

public class HexDataFormat {
    public static final String DEFAULT_DAY_NO_SEPRATOR_FORMAT = "yyyyMMdd";
    // public final
    public final static int NULL_DATA = 0;//null
    public final static int ARRAY = 1;
    public final static int STRUCTURE = 2; //结构体 structure
    public final static int BOOL = 3;
    public final static int BIT_STRING = 4;
    public final static int DOUBLE_LONG = 5; //int32
    public final static int DOUBLE_LONG_UNSIGNED = 6; //u32
    public final static int OCTET_STRING = 9;//octs
    public final static int VISIBLE_STRING = 10; //ascs
    public final static int UTF8_STRING = 12;
    public final static int BCD = 13; //binary coded decimal
    public final static int INTEGER = 15; //integer 8 /int8
    public final static int LONG = 16;   //integer 16 /int16
    public final static int UNSIGNED = 17;// unsigned 8 / u8
    public final static int LONG_UNSIGNED = 18; //unsigned 16 /u16
    public final static int COMPACT_ARRAY = 19;//
    public final static int LONG64 = 20;// integer 64
    public final static int LONG64_UNSIGNED = 21;// unsigned 64 / u64
    public final static int ENUM = 22;
    public final static int FLOAT32 = 23; //octet string(size(4))
    public final static int FLOAT64 = 24; //octet string(size(8))
    public final static int DATE_TIME = 25;//octet string(size(12))
    public final static int DATE = 26;//octet string(size(5))
    public final static int TIME = 27;//`octet string(size(4))
    public final static int BCD_8 = 101; //bcd 8个字节 3个字节 hex  5个字节 DATE

    public final static int OCTET_STRING_ASCS = 30; //octet string ascs
    public final static int GPRS_IP_PORT_APN_STRUCT = 200; //gprs设置  stationIP port apn 结构体数据
    public final static int GPRS_IP = 201;//gprs设置  stationIP port apn 结构体数据
    public final static int GPRS_PORT = 202;//gprs设置  stationIP port apn 结构体数据
    public final static int GPRS_APN = 203;//gprs设置  stationIP port apn 结构体数据

    public final static int GPRS_PDP_NAME_PASSWORD_STRUCT = 210;//gprs 设置 pdpname pdppassword
    public final static int GPRS_PDP_NAME = 211;//ggprs 设置 pdpname
    public final static int GPRS_PDP_PASSWORD = 212; //gprs 设置 pdpname pdppassword

    public final static int MONTH_SETT_DATE = 500;//读写月结算日期

    public final static int DAY_RATE = 600; //日费率 设置

    public final static int FREEZE_CAPTURE = 700;//冻结捕获对象 设置及读取

    public final static int FREEZE_BILLING = 800;//冻结数据 数据块

    public final static int DISPLAY_ARRAY = 900; //轮显 项

    public final static String OCTET_CODING_HEX = "Hex";  //16进制数据
    public final static String OCTET_CODING_ASCS = "Ascs"; //Ascs
    public final static String OCTET_CODING_STRING = "String";//原始数据发送
    public final static String OCTET_CODING_TOKEN = "Token";//token下发

    /**
     * 获取数据类型 字符串转换int
     *
     * @param dataType 字符串类型
     * @return int
     */
    public static int getDataType(String dataType) {
        int recType = -1;
        switch (dataType) {
            case "U8":
                recType = UNSIGNED;
                break;
            case "U16":
                recType = LONG_UNSIGNED;
                break;
            case "U32":
                recType = DOUBLE_LONG_UNSIGNED;
                break;
            case "Octs":
                recType = OCTET_STRING;
                break;
            case "Bool":
                recType = BOOL;
                break;
            case "Array":
                recType = ARRAY;
                break;
            case "Ascs":
                recType = VISIBLE_STRING;
                break;
        }
        return recType;
    }
}
