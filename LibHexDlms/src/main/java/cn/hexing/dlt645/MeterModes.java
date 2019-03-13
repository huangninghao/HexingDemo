package cn.hexing.dlt645;

/**
 * @author caibinglong
 *         date 2019/3/13.
 *         desc 表模式
 */

public class MeterModes {

    public static final String Common = "Normal";
    public static final String Remote = "Remote Mode";
    public static final String Protected = "Power-on";
    public static final String Limit = "On-demand Limit";
    public static final String PP = "Remote&Power-on";


    public static String GetMeterModeText(String val) {
        switch (val) {
            case "00":
                return MeterModes.Common;
            case "01":
                return MeterModes.Remote;
            case "02":
                return MeterModes.Protected;
            case "03":
                return MeterModes.PP;
            default:
                // errorMessage.Add(new NotFiniteNumberException().Message);
                return val;
        }
    }
}
