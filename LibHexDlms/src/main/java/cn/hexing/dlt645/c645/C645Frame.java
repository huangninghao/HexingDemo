package cn.hexing.dlt645.c645;


import cn.hexing.HexStringUtil;
import cn.hexing.dlt645.FrameParameters;
import cn.hexing.dlt645.NotImplementedException;
import cn.hexing.dlt645.SendFrameTypes;
import cn.hexing.dlt645.iprotocol.IFrame;
import cn.hexing.model.HXFramePara;

import static cn.hexing.HexStringUtil.padRight;

/**
 * @author caibinglong
 *         date 2018/12/7.
 *         desc desc
 */

public class C645Frame implements IFrame{

    private boolean hasError = false;
    //private ErrorMessage errorMessage = new ErrorMessage(typeof(C645Frame));
    public byte[] address = new byte[]{(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA};

    private byte controlCode = 0x01;

    private byte[] sendData;
    private byte[] sendDataArea;


//    public Type[] GetCheckFilter(FrameCheckerFilterTypes type) {
//        switch (type) {
//            case FrameCheckerFilterTypes.C645Frame:
//                return new Type[]{typeof(C645FrameChecker)};
//            default:
//                return new Type[0];
//        }
//    }

    @Override
    public void SetFrameParameter(int FrameType, byte[] parameter) {
        switch (FrameType) {
            case FrameParameters.C645Address:
                //System.Diagnostics.Debug.Assert(parameter.Length == 6);
                if (parameter.length != 6) {
                    //errorMessage.AddStringError("Wrong 645 Address Length", "6", parameter.Length.ToString());
                    return;
                }
                address = parameter.clone();
                break;
            case FrameParameters.C645ControlCode:
                controlCode = parameter[0];
                break;
        }
    }

    @Override
    public String GetSendFrame(byte[] data, int sendType) throws NotImplementedException {
        hasError = false;
        switch (sendType) {
            case SendFrameTypes.C645Transmit:
                this.sendData = data.clone();
                FormTransmitDataArea();
                break;

            default:
                throw new NotImplementedException();

        }
        int dataAreaLength = sendDataArea.length;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("68");
        stringBuilder.append(HexStringUtil.bytesToHexString(address));
        stringBuilder.append("68");
        stringBuilder.append(HexStringUtil.byteToString(controlCode));
        stringBuilder.append(String.format("%02X", dataAreaLength));
        stringBuilder.append(HexStringUtil.bytesToHexString(sendDataArea));
        stringBuilder.append(HexStringUtil.makeCheckSum(stringBuilder.toString()));
        stringBuilder.append("16");

        return stringBuilder.toString();
    }
    public static  byte[] getChangeChannel(String no) {
        String headData = "7E000508014348";
        int channel = -1;
         {
            String meterNumber = no;
            int res = 0;
            for (int i = 0; i < meterNumber.length() / 2; i++) {
                res += Integer.valueOf(meterNumber.substring(i * 2, 2 + i * 2), 16);
            }
            if (res > 0XFF) {
                res -= 0X100;
            }
            String result = padRight(String.valueOf(res), 2, '0');
            channel = Integer.valueOf(result, 16);
            channel %= 6;
            channel++;
        }
        String crc;
        switch (channel) {
            case 0:
                crc = "6B";
                break;
            case 1:
                crc = "6A";
                break;
            case 2:
                crc = "69";
                break;
            case 3:
                crc = "68";
                break;
            case 4:
                crc = "67";
                break;
            case 5:
                crc = "66";
                break;
            case 6:
                crc = "65";
                break;
            default:
                crc = "6A";
                break;

        }
        headData = headData + padRight(String.valueOf(channel), 2, '0') + crc;
        byte[] data = HexStringUtil.hexToByte(headData);
        System.out.println("Channel=" + HexStringUtil.bytesToHexString(data));
        return data;
    }

    private void FormTransmitDataArea() {
        sendDataArea = sendData;
    }
}
