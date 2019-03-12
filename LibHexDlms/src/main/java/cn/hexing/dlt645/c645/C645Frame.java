package cn.hexing.dlt645.c645;


import cn.hexing.HexStringUtil;
import cn.hexing.dlt645.FrameParameters;
import cn.hexing.dlt645.NotImplementedException;
import cn.hexing.dlt645.SendFrameTypes;
import cn.hexing.dlt645.iprotocol.IFrame;

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


    private void FormTransmitDataArea() {
        sendDataArea = sendData;
    }
}
