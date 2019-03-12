package cn.hexing.dlt645.zigbee;

import cn.hexing.dlt645.check.IFrameChecker;
import cn.hexing.dlt645.model.ReceiveModel;

/**
 * @author caibinglong
 *         date 2018/12/11.
 *         desc desc
 */

public class RemoteCommandResponseChecker extends ZigBeeFrameChecker {
    private byte[] longAddress;

    public byte[] commandId;

    public byte[] shortAddress;

    public void SetLongAddress(byte[] longAddress) {
        this.longAddress = longAddress.clone();
    }

    public void SetCommandId(byte highByte, byte lowByte) {
        commandId = new byte[2];
        commandId[0] = highByte;
        commandId[1] = lowByte;
    }

    public void SetShortAddress(byte highByte, byte lowByte) {
        shortAddress = new byte[2];
        shortAddress[0] = highByte;
        shortAddress[1] = lowByte;
    }

    public RemoteCommandResponseChecker(int headIndex) {
        super(headIndex);
    }

    @Override
    public ReceiveModel Check(int currentIndex, byte currentByte) {
        ReceiveModel model = super.Check(currentIndex, currentByte);
        if (!model.isFinish) {
            int offset = currentIndex - super.GetDataHeadIndex();
            switch (offset) {
                case 0:
                    if (currentByte != (byte) 0x97) {
                        model.isFinish = true;
                        model.isSuccess = false;
                        model.errorMsg = "Note:Wrong Frame Type=0x97||" + currentByte;
                        return model;
                    }
                    break;
                case 1:
                    if (currentByte != 0x01) {
                        model.isFinish = true;
                        model.isSuccess = false;
                        model.errorMsg = "Note:Wrong Frame Id=0x01||" + currentByte;
                        return model;
                    }
                    break;
                case 2:
                    if (longAddress != null) {
                        if (currentByte != longAddress[0]) {
                            model.isFinish = true;
                            model.isSuccess = false;
                            model.errorMsg = "Note:Wrong longAddress index 0 =" + longAddress[0] + "||curr=" + currentByte;
                            return model;
                        }
                    }
                    break;
                case 3:
                    if (longAddress != null) {
                        if (currentByte != longAddress[1]) {
                            model.isFinish = true;
                            model.isSuccess = false;
                            model.errorMsg = "Note:Wrong longAddress index 1 =" + longAddress[1] + "||curr=" + currentByte;
                            return model;
                        }
                    }
                    break;
                case 4:
                    if (longAddress != null) {
                        if (currentByte != longAddress[2]) {
                            model.isFinish = true;
                            model.isSuccess = false;
                            model.errorMsg = "Note:Wrong longAddress index 2 =" + longAddress[2] + "||curr=" + currentByte;
                            return model;
                        }
                    }
                    break;
                case 5:
                    if (longAddress != null) {
                        if (currentByte != longAddress[3]) {
                            model.isFinish = true;
                            model.isSuccess = false;
                            model.errorMsg = "Note:Wrong longAddress index 3 =" + longAddress[3] + "||curr=" + currentByte;
                            return model;
                        }
                    }
                    break;
                case 6:
                    if (longAddress != null) {
                        if (currentByte != longAddress[4]) {
                            model.isFinish = true;
                            model.isSuccess = false;
                            model.errorMsg = "Note:Wrong longAddress index 4 =" + longAddress[4] + "||curr=" + currentByte;
                            return model;
                        }
                    }
                    break;
                case 7:
                    if (longAddress != null) {
                        if (currentByte != longAddress[5]) {
                            model.isFinish = true;
                            model.isSuccess = false;
                            model.errorMsg = "Note:Wrong longAddress index 5 =" + longAddress[5] + "||curr=" + currentByte;
                            return model;
                        }
                    }
                    break;
                case 8:
                    if (longAddress != null) {
                        if (currentByte != longAddress[6]) {
                            model.isFinish = true;
                            model.isSuccess = false;
                            model.errorMsg = "Note:Wrong longAddress index 6 =" + longAddress[6] + "||curr=" + currentByte;
                            return model;
                        }
                    }
                    break;
                case 9:
                    if (longAddress != null) {
                        if (currentByte != longAddress[7]) {
                            model.isFinish = true;
                            model.isSuccess = false;
                            model.errorMsg = "Note:Wrong longAddress index 7 =" + longAddress[7] + "||curr=" + currentByte;
                            return model;
                        }
                    }
                    break;
                case 10:
                    if (shortAddress != null) {
                        if (currentByte != shortAddress[0]) {
                            model.isFinish = true;
                            model.isSuccess = false;
                            model.errorMsg = "Note:Wrong shortAddress High Byte=" + shortAddress[0] + "||curr=" + currentByte;
                            return model;
                        }
                    }
                    break;
                case 11:
                    if (shortAddress != null) {
                        if (currentByte != shortAddress[1]) {
                            model.isFinish = true;
                            model.isSuccess = false;
                            model.errorMsg = "Note:Wrong shortAddress Low Byte=" + shortAddress[1] + "||curr=" + currentByte;
                            return model;
                        }
                    }
                    break;
                case 12:
                    if (commandId != null) {
                        if (currentByte != commandId[0]) {
                            model.isFinish = true;
                            model.isSuccess = false;
                            model.errorMsg = "Note:Wrong Command Id High Byte=" + shortAddress[1] + "||curr=" + currentByte;
                            return model;
                        }
                    }
                    break;
                case 13:
                    if (commandId != null) {
                        if (currentByte != commandId[1]) {
                            model.isFinish = true;
                            model.isSuccess = false;
                            model.errorMsg = "Note:Wrong Command Id Low Byte=" + shortAddress[1] + "||curr=" + currentByte;
                            return model;
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        return model;
    }

    public int GetDataHeadIndex() {
        return super.GetDataHeadIndex() + 15;
    }

    public int GetDataLength() {
        return super.GetDataLength() - 15;
    }

    public IFrameChecker CreateInstance(int HeadIndex) {
        return new RemoteCommandResponseChecker(HeadIndex);
    }

}
