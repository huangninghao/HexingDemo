package cn.hexing.dlt645;


/**
 * @author caibinglong
 *         date 2019/2/22.
 *         desc 继电器操作原因
 */

public class OperationReasons {
    public static final String MoneyLackingNoOverdraft = "Balance <= 0";
    public static final String MoneyLackingOverdraft = "Overdraft";
    public static final String OverLoadCutOff = "Overload";
    public static final String STSCutOff = "STS Testing";
    public static final String PreOpenMeterCover = "HMCO";
    public static final String PreOpenButtonCover = "HTCO";
    public static final String RemoteCutOff = "Remote Control";
    public static final String ElectricityLarceny = "Tampering";
    public static final String CommonCutOff = "Normal Off";
    public static final String FactoryModelCutOff = "CUFM";
    public static final String NormalUse = "Normal";
    public static final String STSTestSwitch = "STS Testing";
    public static final String ProtectedConnect = "Keep Power-on";
    public static final String CommonConnect = "Normal On";
    public static final String FactoryModelConnect = "OUFM";

    public static final String RelayStatusConnect = "Connect";
    public static final String RelayStatusDisconnect = "Disconnect";

    public static String GetRelayOperationReason(String value) {
        switch (value) {
            case "01":
                return OperationReasons.MoneyLackingNoOverdraft;
            case "02":
                return OperationReasons.MoneyLackingOverdraft;
            case "03":
                return OperationReasons.OverLoadCutOff;
            case "04":
                return OperationReasons.STSCutOff;
            case "05":
                return OperationReasons.PreOpenMeterCover;
            case "06":
                return OperationReasons.PreOpenButtonCover;
            case "07":
                return OperationReasons.RemoteCutOff;
            case "08":
                return OperationReasons.ElectricityLarceny;
            case "09":
                return OperationReasons.CommonCutOff;
            case "0A":
                return OperationReasons.FactoryModelCutOff;
            case "10":
                return OperationReasons.NormalUse;
            case "20":
                return OperationReasons.STSTestSwitch;
            case "30":
                return OperationReasons.ProtectedConnect;
            case "40":
                return OperationReasons.CommonConnect;
            case "50":
                return OperationReasons.FactoryModelConnect;
            default:
                return value;
        }
    }

    /**
     * 继电器状态
     *
     * @param value 16进制数据
     * @return String
     */
    public static String getRelayStatus(String value) {
        switch (value) {
            case "5F":
                return OperationReasons.RelayStatusConnect;
            case "50":
                return OperationReasons.RelayStatusDisconnect;
            default:
                return value;
        }
    }


}
