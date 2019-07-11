package cn.hexing.dlt645.comm;

import java.util.Arrays;

import cn.hexing.HexStringUtil;
import cn.hexing.dlt645.FrameParameters;
import cn.hexing.dlt645.MeterDataTypes;
import cn.hexing.dlt645.NotImplementedException;
import cn.hexing.dlt645.SendFrameTypes;
import cn.hexing.dlt645.c645.C645Frame;
import cn.hexing.dlt645.model.ReceiveModel;
import cn.hexing.iComm.ICommunicator;

/**
 * @author huangninghao
 * date 2019/6/26.
 * desc desc
 */

public class CHX645Meter extends CommOpticalSerialPort implements ICommunicator {

    private byte[] send645Address = BroadCast645Address;


    public CHX645Meter() {
        setSsSendFrameType(SendFrameTypes.C645ZigbeeTransmit);
    }

    public CHX645Meter(String c645Address) {


        if (c645Address != null) {
            byte[] c645AddressBytes = HexStringUtil.hexToByte(c645Address);
            if (c645AddressBytes.length != 6) {
                return;
            }
            this.send645Address = c645AddressBytes;
        }
        setSsSendFrameType(SendFrameTypes.C645ZigbeeTransmit);
        //base.Checker.SetFilter(new Type[]{typeof(C645ZigbeeFrameChecker)});
    }

    public CHX645Meter(byte[] c645AddressBytes) {


        if (c645AddressBytes != null) {
            c645AddressBytes = c645AddressBytes.clone();
            if (c645AddressBytes.length != 6) {
                return;
            }
            this.send645Address = c645AddressBytes;
        }
        setSsSendFrameType(SendFrameTypes.C645ZigbeeTransmit);
        //base.Checker.SetFilter(new Type[]{typeof(C645ZigbeeFrameChecker)});
    }

    @Override
    public ReceiveModel Read(ReceiveModel model, String dateTimeHexString) {
        byte controlCode = 0x1F;
        byte expectControlCode = (byte) 0x9F;

        byte[] sendid = new byte[]{0x02, (byte) 0xE0};
        byte[] sendTypeData =HexStringUtil.reverse(HexStringUtil.GetIntegerBytes(model.getReadType(), 3)) ;
        byte[] sendParaData = HexStringUtil.hexToByte(dateTimeHexString);

        int sendLen = sendid.length + sendTypeData.length + sendParaData.length;
        model.controlCode = controlCode;
        model.expectControlCode = expectControlCode;
        model.sendData = new byte[sendLen];

        System.arraycopy(sendid, 0, model.sendData, 0, sendid.length);

        System.arraycopy(sendTypeData, 0, model.sendData,  sendid.length, sendTypeData.length);
        System.arraycopy(sendParaData, 0, model.sendData, sendTypeData.length+ sendid.length, sendParaData.length);
        //model.maxWaitTime = 8000;
        model = TransmitData(model);
        if (!model.isSuccess) {
            model.errorMsg = "Wrong Return Control Code=" + expectControlCode + "||controlCode=" + controlCode;
        }
        return model;
    }

    @Override
    public ReceiveModel ReadDay(ReceiveModel model, String dateTimeHexString) {

        byte controlCode = (byte) 0x01;
        byte expectControlCode = (byte) 0x81; //68 81 结束
        byte exeControlCode = (byte) 0xA1; //68 A1后续帧标识
        byte[] sendTypeData = HexStringUtil.GetIntegerBytes(model.getReadType(), 2);
        byte[] sendParaData = HexStringUtil.hexToByte(dateTimeHexString);
        int sendLen = sendTypeData.length + sendParaData.length;
        model.controlCode = controlCode;
        model.sendData = new byte[sendLen];
        System.arraycopy(sendTypeData, 0, model.sendData, 0, sendTypeData.length);
        System.arraycopy(sendParaData, 0, model.sendData, sendTypeData.length, sendParaData.length);

        //model.maxWaitTime = 5000;
        model.expectControlCode = expectControlCode;
        model.controlCode = controlCode;

        model = TransmitData(model);
        byte[] received;
        if (model.isSuccess) {
            received = Arrays.copyOf(model.recBytes, model.recBytes.length);
            for (int i = 0; i < 5; i++) {
                sendTypeData = HexStringUtil.GetIntegerBytes(model.getReadType(), 2);
                // byte[] datalenn = HexStringUtil.GetIntegerBytes(3, 2);
                sendParaData = HexStringUtil.hexToByte(dateTimeHexString);
                sendLen = sendTypeData.length + sendParaData.length;
                model.controlCode = controlCode;
                model.sendData = new byte[sendLen];
                System.arraycopy(sendTypeData, 0, model.sendData, 0, sendTypeData.length);
                System.arraycopy(sendParaData, 0, model.sendData, sendTypeData.length, sendParaData.length);
                model = TransmitData(model);

                if (model.isSuccess) {
                    int len = received.length;
                    received = new byte[received.length + model.recBytes.length];
                    System.arraycopy(model.recBytes, 0, received, len, received.length);
                }
            }
            model.recBytes = Arrays.copyOf(received, received.length);
        }
        return model;
    }

    @Override
    public ReceiveModel ContinueRead(@MeterDataTypes.ReadDataTypes int type, String dateTimeHexString) {
        return new ReceiveModel();
    }

    @Override
    public ReceiveModel Read(ReceiveModel model) {
        return Read(model, "");
    }

    @Override
    public ReceiveModel Write(@MeterDataTypes.ReadDataTypes int type, byte[] passwordBytes, byte[] writeData, ReceiveModel model) {
        byte controlCode = 0x04;
        byte expectControlCode = (byte) 0x84;
        byte[] sendTypeData = HexStringUtil.GetIntegerBytes(type, 2);
        int sendLen = sendTypeData.length + writeData.length + passwordBytes.length;
        model.controlCode = controlCode;
        model.expectControlCode = expectControlCode;
        model.sendData = new byte[sendLen];
        System.arraycopy(sendTypeData, 0, model.sendData, 0, sendTypeData.length);
        System.arraycopy(passwordBytes, 0, model.sendData, sendTypeData.length, passwordBytes.length);
        System.arraycopy(writeData, 0, model.sendData, sendTypeData.length + passwordBytes.length, writeData.length);

        model = TransmitData(model);
        if (model.isSuccess) {
            model.errorMsg = "Wrong Return Control Code=" + expectControlCode + "||ControlCode=" + controlCode;
        }
        return model;
    }

    @Override
    public void SetParameters(@FrameParameters.FParameters int type, byte[] value) {
        switch (type) {
            case FrameParameters.C645Address:
                if (value.length != 6) {
                    return;
                }
                this.send645Address = value.clone();
                break;

            default:
                break;
        }
    }

    private ReceiveModel TransmitData(ReceiveModel model) {
        C645Frame c645Frame = new C645Frame();
        //base.Checker.SetFilter(base.SendFrame.GetCheckFilter(FrameCheckerFilterTypes.C645ZigbeeReceivedData));
        //base.Checker.ClearFrameParameters();
        byte[] sendData = model.sendData.clone();
        sendData = PreHandleSendData(sendData);

        c645Frame.SetFrameParameter(FrameParameters.C645ControlCode, new byte[]{model.controlCode});
        c645Frame.SetFrameParameter(FrameParameters.C645Address, this.send645Address);


        //base.Checker.SetFrameParameter(FrameParameters.C645ControlCode, new byte[0]);
        byte[] sendBytes = new byte[0];
        try {
            sendBytes = HexStringUtil.hexToByte(c645Frame.GetSendFrame(sendData, SendFrameTypes.C645Transmit));
        } catch (NotImplementedException e) {
            e.printStackTrace();
        }
        model.isSuccess = false;
        model.isSend = sendByt(sendBytes);
        if (model.isSend) {
            model.recBytes = receiveByt(model.sleepTime, model.maxWaitTime, model.receiveByteLen);
            if (model.recBytes.length > 0) {
                //controlCode = base.Checker.GetFrameParameter(FrameParameters.C645ControlCode)[0];
                byte[] copyByte = Arrays.copyOf(model.recBytes, model.recBytes.length);
                byte[] temp = new byte[0];
                for (int j = 0; j < copyByte.length; j++) {
                    if (((copyByte[j] & 0xff) == 0x68) &&
                            (copyByte[j + 1] == model.expectControlCode)) {
                        int len = copyByte[j + 2] & 0xff;//表字节长度
                        model.isSuccess = true;
                        if (len > 0) {
                            temp = new byte[len];
                            System.arraycopy(copyByte, j + 3, temp, 0, len);
                        } else {
                            model.recBytes = new byte[0];
                        }
                        break;
                    }
                }
                model.recBytes = PreHandleReceiveData(temp);
            }
        }

        return model;
    }

    @Override
    public void GoodBye() {
        super.closeDevice();
    }
}
