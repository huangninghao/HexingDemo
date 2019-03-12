package cn.hexing.dlt645;

import cn.hexing.HexStringUtil;
import cn.hexing.model.HXFramePara;
import cn.hexing.model.TranXADRAssist;

/**
 * @author caibinglong
 *         date 2018/12/13.
 *         desc desc
 */

public class OrganizationAnalysis {

    public static byte[] MeterPasswordBytes = new byte[]{0x00, 0x00, 0x00, 0x00};
    public static byte[] CollectorPasswordBytes = new byte[]{0x00, 0x00, 0x00, 0x00};

    /**
     * zigBee 数据拼接
     *
     * @param assist TranXADRAssist
     * @return string
     */
    public static String ZigBeeGetXADRCode(TranXADRAssist assist) {
        StringBuilder stringBuilder = new StringBuilder();
        if (assist.c645Bean == null){
            return "";
        }
        switch (assist.c645Bean.getMeterDataType645Id()) {
            case MeterDataTypes.SetupMode:
                stringBuilder.append("04");
                stringBuilder.append("84");
                stringBuilder.append(String.format("%04x", MeterDataTypes.SetupMode));
                stringBuilder.append(HexStringUtil.bytesToHexString(CollectorPasswordBytes));
                stringBuilder.append(assist.writeData);
                break;
            case MeterDataTypes.ReadSetupInfo:
                stringBuilder.append("01");
                stringBuilder.append("81");
                stringBuilder.append(String.format("%04x", MeterDataTypes.ReadSetupInfo));
                break;
            case MeterDataTypes.ReadSetupInfo2:
                stringBuilder.append("01");
                stringBuilder.append("81");
                stringBuilder.append(String.format("%04x", MeterDataTypes.ReadSetupInfo2));
                break;
            case MeterDataTypes.RelayOn:
                break;
            case MeterDataTypes.RelayOff:
                break;
            default:
                stringBuilder.append(assist.writeData);
                break;
        }

        return stringBuilder.toString();
    }

    /**
     * 645协议数据拼接
     *
     * @param assist TranXADRAssist
     * @param para   HXFramePara
     * @return String
     */
    public static String C645GetXADRCode(TranXADRAssist assist, HXFramePara para) {
        StringBuilder stringBuilder = new StringBuilder();
        switch (para.sendType) {
            case SendFrameTypes.C645ZigbeeTransmit:

//            base.SendFrame.SetFrameParameter(FrameParameters.C645ControlCode, new byte[] { controlCode });
//            base.SendFrame.SetFrameParameter(FrameParameters.C645Address, this.send645Address);
//            base.SendFrame.SetFrameParameter(FrameParameters.ZigbeeLongAddress, this.sendZigbeeLongAddress);
//            base.SendFrame.SetFrameParameter(FrameParameters.ZigbeeShortAddress, this.sendZigbeeShortAddress);
                break;
        }
        return stringBuilder.toString();
    }

}
