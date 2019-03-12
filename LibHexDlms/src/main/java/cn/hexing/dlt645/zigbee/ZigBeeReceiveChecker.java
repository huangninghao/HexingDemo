package cn.hexing.dlt645.zigbee;


import cn.hexing.dlt645.FrameParameters;
import cn.hexing.dlt645.check.IFrameChecker;
import cn.hexing.dlt645.model.ReceiveModel;

/**
 * @author caibinglong
 *         date 2018/12/10.
 *         desc desc
 */

public class ZigBeeReceiveChecker extends ZigBeeFrameChecker {

    private byte[] receivedLongAddress = new byte[8];
    private byte[] receivedShortAddress = new byte[2];
    public byte[] longAddress;
    public byte[] shortAddress;

    public ZigBeeReceiveChecker(int headIndex) {
        super(headIndex);
    }

    public IFrameChecker CreateInstance(int HeadIndex) {
        return new ZigBeeReceiveChecker(HeadIndex);
    }

    public void SetLongAddress(byte[] longAddress) {
        this.longAddress = longAddress.clone();
    }

    public void SetShortAddress(byte highByte, byte lowByte) {
        shortAddress = new byte[2];
        shortAddress[0] = highByte;
        shortAddress[1] = lowByte;
    }

    public ReceiveModel Check(int currentIndex, byte currentByte) {
        ReceiveModel receiveModel = super.Check(currentIndex, currentByte);
        if (!receiveModel.isFinish) {
            int offset = currentIndex - super.GetHeadIndex() - 3;
            switch (offset) {
                case 0:
                    if (currentByte != (byte) 0x90) {
                        receiveModel.isFinish = true;
                        receiveModel.isSuccess = false;
                        receiveModel.errorMsg = "Note:Wrong Frame Type= 0x90||curr=" + currentByte;
                        return receiveModel;
                    }
                    break;
                case 1:
                    if (longAddress != null) {
                        if (currentByte != longAddress[0]) {
                            receiveModel.isFinish = true;
                            receiveModel.isSuccess = false;
                            receiveModel.errorMsg = "Note:Wrong longAddress index 0 =" + longAddress[0] + "||curr=" + currentByte;
                            return receiveModel;
                        }
                    } else {
                        receivedLongAddress[0] = currentByte;
                    }
                    break;
                case 2:
                    if (longAddress != null) {
                        if (currentByte != longAddress[1]) {
                            receiveModel.isFinish = true;
                            receiveModel.isSuccess = false;
                            receiveModel.errorMsg = "Note:Wrong longAddress index 1 =" + longAddress[1] + "||curr=" + currentByte;
                            return receiveModel;
                        }
                    } else {
                        receivedLongAddress[1] = currentByte;
                    }
                    break;
                case 3:
                    if (longAddress != null) {
                        if (currentByte != longAddress[2]) {
                            receiveModel.isFinish = true;
                            receiveModel.isSuccess = false;
                            receiveModel.errorMsg = "Note:Wrong longAddress index 2 =" + longAddress[2] + "||curr=" + currentByte;
                            return receiveModel;
                        }
                    } else {
                        receivedLongAddress[2] = currentByte;
                    }
                    break;
                case 4:
                    if (longAddress != null) {
                        if (currentByte != longAddress[3]) {
                            receiveModel.isFinish = true;
                            receiveModel.isSuccess = false;
                            receiveModel.errorMsg = "Note:Wrong longAddress index 3 =" + longAddress[3] + "||curr=" + currentByte;
                            return receiveModel;
                        }
                    } else {
                        receivedLongAddress[3] = currentByte;
                    }
                    break;
                case 5:
                    if (longAddress != null) {
                        if (currentByte != longAddress[4]) {
                            receiveModel.isFinish = true;
                            receiveModel.isSuccess = false;
                            receiveModel.errorMsg = "Note:Wrong longAddress index 4 =" + longAddress[4] + "||curr=" + currentByte;
                            return receiveModel;
                        }
                    } else {
                        receivedLongAddress[4] = currentByte;
                    }
                    break;
                case 6:
                    if (longAddress != null) {
                        if (currentByte != longAddress[5]) {
                            receiveModel.isFinish = true;
                            receiveModel.isSuccess = false;
                            receiveModel.errorMsg = "Note:Wrong longAddress index 5 =" + longAddress[5] + "||curr=" + currentByte;
                            return receiveModel;
                        }
                    } else {
                        receivedLongAddress[5] = currentByte;
                    }
                    break;
                case 7:
                    if (longAddress != null) {
                        if (currentByte != longAddress[6]) {
                            receiveModel.isFinish = true;
                            receiveModel.isSuccess = false;
                            receiveModel.errorMsg = "Note:Wrong longAddress index 6 =" + longAddress[6] + "||curr=" + currentByte;
                            return receiveModel;
                        }
                    } else {
                        receivedLongAddress[6] = currentByte;
                    }
                    break;
                case 8:
                    if (longAddress != null) {
                        if (currentByte != longAddress[7]) {
                            receiveModel.isFinish = true;
                            receiveModel.isSuccess = false;
                            receiveModel.errorMsg = "Note:Wrong longAddress index 7 =" + longAddress[7] + "||curr=" + currentByte;
                            return receiveModel;
                        }
                    } else {
                        receivedLongAddress[7] = currentByte;
                    }
                    break;
                case 9:
                    if (shortAddress != null) {
                        if (currentByte != shortAddress[0]) {
                            receiveModel.isFinish = true;
                            receiveModel.isSuccess = false;
                            receiveModel.errorMsg = "Note:Wrong shortAddress High Byte=" + shortAddress[0] + "||curr=" + currentByte;
                            return receiveModel;
                        }
                    } else {
                        receivedShortAddress[0] = currentByte;
                    }
                    break;
                case 10:
                    if (shortAddress != null) {
                        if (currentByte != shortAddress[1]) {
                            receiveModel.isFinish = true;
                            receiveModel.isSuccess = false;
                            receiveModel.errorMsg = "Note:Wrong shortAddress Low Byte=" + shortAddress[1] + "||curr=" + currentByte;
                            return receiveModel;
                        }
                    } else {
                        receivedShortAddress[1] = currentByte;
                    }
                    break;

                default:
                    break;
            }

        }
        return receiveModel;
    }

    public int GetDataHeadIndex() {
        return super.GetDataHeadIndex() + 12;
    }

    public int GetDataLength() {
        return super.GetDataLength() - 12;
    }

    @Override
    public void SetFrameCheckerParameter(int type, byte[] parameter) {
        switch (type) {
            case FrameParameters.ZigbeeLongAddress:
                this.SetLongAddress(parameter);
                break;
            case FrameParameters.ZigbeeShortAddress:
                this.SetShortAddress(parameter[0], parameter[1]);
                break;
        }
    }

    @Override
    public byte[] GetReceivedFrameParameter(int type) {
        switch (type) {
            case FrameParameters.ZigbeeLongAddress:
                return this.receivedLongAddress.clone();
            case FrameParameters.ZigbeeShortAddress:
                return this.receivedShortAddress.clone();
        }
        return new byte[0];
    }
}
