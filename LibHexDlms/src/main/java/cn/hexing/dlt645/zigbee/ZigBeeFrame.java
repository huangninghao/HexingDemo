package cn.hexing.dlt645.zigbee;

import java.lang.reflect.Type;

import cn.hexing.HexStringUtil;
import cn.hexing.dlt645.FrameParameters;
import cn.hexing.dlt645.NotImplementedException;
import cn.hexing.dlt645.SendFrameTypes;
import cn.hexing.dlt645.check.FrameCheckerFilterTypes;
import cn.hexing.dlt645.iprotocol.IFrame;

/**
 * @author caibinglong
 *         date 2018/12/7.
 *         desc desc
 */

public class ZigBeeFrame implements IFrame {
    public byte[] getLongAddress() {
        return longAddress;
    }

    public void setLongAddress(byte[] longAddress) {
        this.longAddress = longAddress;
    }

    public byte[] getShortAddress() {
        return shortAddress;
    }

    public void setShortAddress(byte[] shortAddress) {
        this.shortAddress = shortAddress;
    }

    private byte[] longAddress = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xFF, (byte) 0xFF};
    private byte[] shortAddress = new byte[]{(byte) 0xFF, (byte) 0xFF};
    private byte[] commandId;
    private byte[] sendData;
    private byte[] sendDataArea;
    private boolean hasError;

    @Override
    public void SetFrameParameter(int type, byte[] parameter) throws NotImplementedException {
        switch (type) {
            case FrameParameters.ZigbeeCommandId:
                if (parameter.length != 2) {
                    //errorMessage.AddStringError("Wrong Command Id Length", "2", parameter.Length.ToString());
                    return;
                }
                commandId = parameter.clone();
                break;
            case FrameParameters.ZigbeeLongAddress:
                if (parameter.length != 8) {
                    //errorMessage.AddStringError("Wrong Long Address Length", "8", parameter.length.ToString());
                    return;
                }
                longAddress = parameter.clone();
                break;
            case FrameParameters.ZigbeeShortAddress:
                if (parameter.length != 2) {
                    //errorMessage.AddStringError("Wrong Short Address Length", "2", parameter.Length.ToString());
                    return;
                }
                shortAddress = parameter.clone();
                break;
            case FrameParameters.ZigbeeNetWork:
                if (parameter.length != 29) {
                    //errorMessage.AddStringError("Wrong Short Address Length", "29", parameter.Length.ToString());
                    return;
                }
                sendDataArea = parameter.clone();
                break;
            default:
                throw new NotImplementedException();
        }
    }

    @Override
    public String GetSendFrame(byte[] data, int sendType) throws NotImplementedException {
        hasError = false;
        this.sendData = data;
        StringBuilder stringBuilder = new StringBuilder();
        switch (sendType) {
            case SendFrameTypes.ZigbeeTransmit:
                FormTransmitDataArea();
                break;
            case SendFrameTypes.ZigbeeATCommand:
                FormCommandDataArea();
                break;
            case SendFrameTypes.ZigbeeRemoteATCommand:
                FormRemoteCommandDataArea();
                break;
            case SendFrameTypes.ZigbeeNetworkSearchCommand:
                FormNetworkSearchDataArea();
                break;
            default:
                throw new NotImplementedException();

        }
        if (!hasError) {
            int dataAreaLength = sendDataArea.length;
            stringBuilder.append("7E");
            stringBuilder.append(String.format("%02X", dataAreaLength >> 8));
            stringBuilder.append(String.format("%02X", dataAreaLength & 0xFF));
            stringBuilder.append(HexStringUtil.bytesToHexString(sendDataArea));
            stringBuilder.append(HexStringUtil.checkSum2(sendDataArea));
        }
        return stringBuilder.toString().toUpperCase();
    }

    private void FormRemoteCommandDataArea() {
        if (!CheckLongAddress() || !CheckShortAddress() || !CheckCommandId() || !CheckSendData()) {
            hasError = true;
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("17");
        stringBuilder.append("01");
        stringBuilder.append(HexStringUtil.bytesToHexString(longAddress));
        stringBuilder.append(HexStringUtil.bytesToHexString(shortAddress));
        stringBuilder.append("00");
        stringBuilder.append(HexStringUtil.bytesToHexString(commandId));
        stringBuilder.append(HexStringUtil.bytesToHexString(sendData));
        sendDataArea = HexStringUtil.hexToByte(stringBuilder.toString());
    }

    private void FormCommandDataArea() {
        if (!CheckCommandId() || !CheckSendData()) {
            hasError = true;
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("08");
        stringBuilder.append("01");
        stringBuilder.append(HexStringUtil.bytesToHexString(commandId));
        stringBuilder.append(HexStringUtil.bytesToHexString(sendData));
        sendDataArea = HexStringUtil.hexToByte(stringBuilder.toString());
    }

    private void FormTransmitDataArea() {
        if (!CheckLongAddress() || !CheckShortAddress() || !CheckSendData()) {
            hasError = true;
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("10");
        stringBuilder.append("01");
        stringBuilder.append(HexStringUtil.bytesToHexString(longAddress));
        stringBuilder.append(HexStringUtil.bytesToHexString(shortAddress));
        stringBuilder.append("00");// 广播半径暂设为默认值
        stringBuilder.append("00");// Option暂设为默认值
        stringBuilder.append(HexStringUtil.bytesToHexString(sendData));
        sendDataArea = HexStringUtil.hexToByte(stringBuilder.toString());
    }

    private void FormNetworkSearchDataArea() {
        sendDataArea = new byte[]{0x11, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                0x00, 0x00, 0x00, 0x26, 0x00, 0x00, 0x00, 0x00, (byte) 0xAA, 0x05, 0x00, 0x26, 0x00, (byte) 0xF8, (byte) 0xFF, 0x07, 0x03};
    }

    private boolean CheckLongAddress() {
        if (longAddress == null || longAddress.length != 8) {
            //errorMessage.AddStringError("Wrong long address length ", "8", longAddress.Length.ToString());
            return false;
        }
        return true;
    }

    private boolean CheckShortAddress() {
        if (shortAddress == null || shortAddress.length != 2) {
            //errorMessage.AddStringError("Wrong short address length ", "2", shortAddress.Length.ToString());
            return false;
        }
        return true;
    }

    private boolean CheckCommandId() {
        if (commandId == null || commandId.length != 2) {
            //errorMessage.AddStringError("Wrong Command ID length ", "2", shortAddress.Length.ToString());
            return false;
        }
        return true;
    }

    private boolean CheckSendData() {
        if (sendData == null) {
            sendData = new byte[0];
        }
        if (sendData.length >= 65500) {
            //errorMessage.AddStringError("Wrong Data length ", "0-65500", sendData.Length.ToString());
            return false;
        }
        return true;
    }

    public int GetCheckFilter(@FrameCheckerFilterTypes.FCheckerFilterTypes int type) {
//        switch (type) {
//            case FrameCheckerFilterTypes.ZigbeeCommandResponse:
//                return new Type[]{typeof(CommandResponseChecker)};
//            case FrameCheckerFilterTypes.ZigbeeTransmitResponse:
//                return new Type[]{typeof(TransmitResponseChecker)};
//            case FrameCheckerFilterTypes.ZigbeeReceivedData:
//                return new Type[]{typeof(ZigBeeReceiveChecker)};
//            case FrameCheckerFilterTypes.ZigbeeRemoteCommandResponse:
//                return new Type[]{typeof(RemoteCommandResponseChecker)};
//            case FrameCheckerFilterTypes.ZigbeeCommandResponsedStateReport:
//                return new Type[]{typeof(CommandResponseChecker), typeof(StateReportChecker)};
//            case FrameCheckerFilterTypes.ZigbeeTransmitResponsedReceivedData:
//                return new Type[]{typeof(TransmitResponseChecker), typeof(ZigBeeReceiveChecker)};
//            case FrameCheckerFilterTypes.ZigbeeNetworkSearchResponse:
//                return new Type[]{typeof(NetworkSearchResponseChecker)};
//            default:
//                return new Type[0];
//        }
        return 0;
    }

}
