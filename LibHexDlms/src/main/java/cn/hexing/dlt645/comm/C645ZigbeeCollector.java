package cn.hexing.dlt645.comm;

import java.util.Arrays;

import cn.hexing.HexStringUtil;
import cn.hexing.dlt645.FrameParameters;
import cn.hexing.dlt645.MeterDataTypes;
import cn.hexing.dlt645.NotImplementedException;
import cn.hexing.dlt645.SendFrameTypes;
import cn.hexing.dlt645.c645.C645ZigbeeFrame;
import cn.hexing.dlt645.model.ReceiveModel;
import cn.hexing.iComm.ICommunicator;


/**
 * @author caibinglong
 *         date 2018/12/16.
 *         desc 645 zig 采集器操作
 */

public class C645ZigbeeCollector extends CommOpticalSerialPort implements ICommunicator {

    private byte[] send645Address = CommOpticalSerialPort.BroadCast645Address;
    private byte[] sendZigbeeLongAddress = CommOpticalSerialPort.BroadCastZigbeeAddress;
    private byte[] sendZigbeeShortAddress = CommOpticalSerialPort.BroadCastZigbeeShortAddress;

    public C645ZigbeeCollector() {

    }

    public C645ZigbeeCollector(String longAddress, String shortAddress, String c645Address) {
        if (longAddress != null) {
            byte[] longAddressBytes = HexStringUtil.hexToByte(longAddress);
            if (longAddressBytes.length != 8) {
                return;
            }
            this.sendZigbeeLongAddress = longAddressBytes;
        }
        if (shortAddress != null) {
            byte[] shortAddressBytes = HexStringUtil.hexToByte(shortAddress);
            if (shortAddressBytes.length != 2) {
                return;
            }
            this.sendZigbeeShortAddress = shortAddressBytes;
        }

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

    public C645ZigbeeCollector(byte[] longAddressBytes, byte[] shortAddressBytes, byte[] c645AddressBytes) {
        if (longAddressBytes != null) {
            longAddressBytes = longAddressBytes.clone();
            if (longAddressBytes.length != 8) {
                return;
            }
            this.sendZigbeeLongAddress = longAddressBytes;
        }
        if (shortAddressBytes != null) {
            shortAddressBytes = shortAddressBytes.clone();
            if (shortAddressBytes.length != 2) {
                return;
            }
            this.sendZigbeeShortAddress = shortAddressBytes;
        }

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
        byte controlCode = 0x01;
        byte expectControlCode = (byte) 0x81;
        byte[] sendTypeData = HexStringUtil.GetIntegerBytes(model.getReadType(), 2);
        byte[] sendParaData = HexStringUtil.hexToByte(dateTimeHexString);

        int sendLen = sendTypeData.length + sendParaData.length;
        model.controlCode = controlCode;
        model.expectControlCode = expectControlCode;
        model.sendData = new byte[sendLen];
        System.arraycopy(sendTypeData, 0, model.sendData, 0, sendTypeData.length);
        System.arraycopy(sendParaData, 0, model.sendData, sendTypeData.length, sendParaData.length);

        model = TransmitData(model);
        return model;
    }

    @Override
    public ReceiveModel ReadDay(ReceiveModel model, String dateTimeHexString) {
        byte controlCode = (byte) 0x01;
        byte expectControlCode = (byte) 0x81;
        byte exeControlCode = (byte) 0xA1;
        byte[] sendTypeData = HexStringUtil.GetIntegerBytes(model.getReadType(), 2);
        byte[] receivedData = new byte[0];

        boolean isOK = true;
        int dataBlockNum = 0;
        int num = 0;
        byte[] tempByte;
        while (isOK) {
            byte[] dataLen = HexStringUtil.GetIntegerBytes(dataBlockNum, 2);
            byte[] sendParaData = HexStringUtil.hexToByte(dateTimeHexString);
            model.sendData = new byte[0];
            model.sendData = HexStringUtil.addBytes(model.sendData, sendTypeData);

            model.sendData = HexStringUtil.addBytes(model.sendData, dataLen);

            model.sendData = HexStringUtil.addBytes(model.sendData, sendParaData);

            model.expectControlCode = expectControlCode;
            model.controlCode = controlCode;
            model.exeControlCode = exeControlCode;

            model = TransmitData(model);
            if (model.isSuccess && model.recBytes.length > 0) {

                if (model.controlCode == model.exeControlCode) {
                    receivedData = HexStringUtil.addBytes(receivedData, model.recBytes);
                    isOK = true;
                    dataBlockNum++;
                }
                if (model.controlCode == model.expectControlCode) {
                    /////////////////////////////////////////////////
                    //add by yfb 2016-10-10
                    //新增协议解析，拼接情况，去掉第二帧（甚至更多）数据区前5个字节 (0xB0 0xF0 0x00 0x00 0x14)
                    tempByte = model.recBytes;
                    if (num > 0) {
                        tempByte = HexStringUtil.removeBytes(tempByte, 0, 4);
                    }
                    receivedData = HexStringUtil.addBytes(receivedData, tempByte);
                    isOK = false;
                }
                num++;
            } else {
                isOK = false;
            }
        }

        model.recBytes = receivedData;
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
        model.setReadType(type);
        model.controlCode = controlCode;
        //model.maxWaitTime = 10 * 1000;
        model.sendData = new byte[sendLen];
        model.expectControlCode = expectControlCode;
        System.arraycopy(sendTypeData, 0, model.sendData, 0, sendTypeData.length);
        System.arraycopy(passwordBytes, 0, model.sendData, sendTypeData.length, passwordBytes.length);
        System.arraycopy(writeData, 0, model.sendData, sendTypeData.length + passwordBytes.length, writeData.length);

        return TransmitData(model);
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
            case FrameParameters.ZigbeeLongAddress:
                if (value.length != 8) {
                    return;
                }
                this.sendZigbeeLongAddress = value.clone();
                break;
            case FrameParameters.ZigbeeShortAddress:
                if (value.length != 2) {
                    return;
                }
                this.sendZigbeeShortAddress = value.clone();
                break;
            default:
                break;
        }
    }

    @Override
    public void GoodBye() {
        super.closeDevice();
    }

    private ReceiveModel TransmitData(ReceiveModel model) {
        ssSendFrame = new C645ZigbeeFrame();
        ssSendFrameType = SendFrameTypes.C645ZigbeeTransmit;
        //base.Checker.SetFilter(base.SendFrame.GetCheckFilter(FrameCheckerFilterTypes.C645ZigbeeReceivedData));
        //base.Checker.ClearFrameParameters();
        byte[] sendData = model.sendData.clone();
        sendData = PreHandleSendData(sendData);
        try {
            ssSendFrame.SetFrameParameter(FrameParameters.C645ControlCode, new byte[]{model.controlCode});
            ssSendFrame.SetFrameParameter(FrameParameters.C645Address, this.send645Address);
            ssSendFrame.SetFrameParameter(FrameParameters.ZigbeeLongAddress, this.sendZigbeeLongAddress);
            ssSendFrame.SetFrameParameter(FrameParameters.ZigbeeShortAddress, this.sendZigbeeShortAddress);
            //ssSendFrame.SetFrameParameter(FrameParameters.C645ControlCode, new byte[0]);
            model.sendData = HexStringUtil.hexToByte(ssSendFrame.GetSendFrame(sendData, ssSendFrameType));
            model.isSend = sendByt(model.sendData);
            if (model.isSend) {
                model.recBytes = receiveByt(model.sleepTime, model.maxWaitTime, model.receiveByteLen);
                byte[] meterByte = new byte[0];
                switch (model.getReadType()) {
                    case MeterDataTypes.ReadEthernet_v1:
                    case MeterDataTypes.PanID:
                    case MeterDataTypes.ReadGPRS:
                    case MeterDataTypes.ReadSetupInfo:
                    case MeterDataTypes.ReadSetupInfo2:
                    case MeterDataTypes.ReadInstantaneous:
                    case MeterDataTypes.ReadInstantaneous_M:
                    case MeterDataTypes.SetupMode:
                    case MeterDataTypes.ReadDayBill:
                    case MeterDataTypes.ReadPre:
                        if (model.recBytes.length > 10) {
                            // 7E 0007 8B01 0000 00 00 00 73  回复第一帧
                            // 注： 8B01  (Frame Type： 定值0x8B Frame ID:  定值0x01)
                            // 0000 目标地址
                            // 00（重发次数）
                            // 00 传输状态反馈 00代表成功
                            // 00(地址发现状态)  0x00 = No Discovery Overhead 0x01 = Address Discovery 0x02 = Route Discovery 0x03 = Address and Route 0x40 = Extended Timeout Discovery
                            // 73（校验和）

                            //Rcv:7E 00 18 90 FF FF FF FF FF FF FF FF 00 00 01 68 AA AA AA AA AA AA 68 81 00 4D 16 C6 回复第二帧
                            int len = model.recBytes.length;
                            boolean isSucc = false;
                            for (int i = 0; i < len; i++) {
                                //zigbee协议验证
                                if (((model.recBytes[i] & 0xff) == 0x8B) && ((model.recBytes[i + 1] & 0xff) == 0x01)
                                        && ((model.recBytes[i + 6] & 0xff) == 0x00)) {
                                    //copyByte = new byte[model.recBytes.length - (i + 8)];
                                    //System.arraycopy(model.recBytes, i + 8, copyByte, 0, model.recBytes.length - (i + 8));
                                    isSucc = true;
                                    break;
                                }
                            }
                            if (isSucc) {
                                byte[] copyByte = Arrays.copyOf(model.recBytes, model.recBytes.length);
                                // int n = model.controlCode == 0x04 ? 0x84 : 0x81; //0x04 是write
                                //645 协议 验证
                                for (int j = 0; j < copyByte.length; j++) {
                                    if ((copyByte[j] & 0xff) == 0x68) {
                                        if (copyByte[j + 1] == model.expectControlCode) {
                                            model.controlCode = model.expectControlCode;
                                            len = copyByte[j + 2] & 0xff;//表字节长度
                                            meterByte = HexStringUtil.getBytes(copyByte, j + 3, len);
                                            model.isSuccess = true;
                                            break;
                                        } else if (copyByte[j + 1] == model.exeControlCode) {
                                            //0xA1
                                            model.controlCode = model.exeControlCode;
                                            len = copyByte[j + 2] & 0xff;//表字节长度
                                            meterByte = HexStringUtil.getBytes(copyByte, j + 3, len);
                                            model.isSuccess = true;
                                            break;
                                        }
                                    }
                                }

                            } else {
                                model.recBytes = new byte[0];
                            }
                            if (meterByte.length > 0) {
                                PreHandleReceiveData(meterByte);
                                model.recBytes = meterByte;
                            } else {
                                model.recBytes = new byte[0];
                            }
                        } else {
                            model.recBytes = new byte[0];
                        }
                        break;

                }
            }

        } catch (NotImplementedException e) {
            e.printStackTrace();
            model.errorMsg = e.getMessage();
        }
        return model;
    }
}
