package cn.hexing.dlt645.zigbee;

import cn.hexing.dlt645.check.IFrameChecker;
import cn.hexing.dlt645.model.ReceiveModel;

/**
 * @author caibinglong
 *         date 2018/12/10.
 *         desc desc
 */

public class CommandResponseChecker extends ZigBeeFrameChecker {
    private int dataHeadIndex = 0;

    private byte[] commandId;

    public void SetCommandId(byte highByte, byte lowByte) {
        commandId = new byte[2];
        commandId[0] = highByte;
        commandId[1] = lowByte;
    }

    public ReceiveModel Check(int currentIndex, byte currentByte) {
        ReceiveModel receiveModel = super.Check(currentIndex, currentByte);
        if (!receiveModel.isFinish) {
            int offset = currentIndex - super.GetHeadIndex() - 3;
            switch (offset) {
                case 0:
                    if (currentByte != (byte) 0x88) {
                        receiveModel.isFinish = true;
                        receiveModel.isSuccess = false;
                        receiveModel.errorMsg = "Note:Wrong Frame Type|| 0x88||" + currentByte;
                    }
                    break;
                case 1:
                    if (currentByte != 0x01) {
                        receiveModel.isFinish = true;
                        receiveModel.isSuccess = false;
                        receiveModel.errorMsg = "Note:Wrong Frame Type|| 0x01||" + currentByte;
                    }
                    break;
                case 2:
                    if (commandId != null) {
                        if (currentByte != commandId[0]) {
                            receiveModel.isFinish = true;
                            receiveModel.isSuccess = false;
                            receiveModel.errorMsg = "Note:Wrong CommandId High Byte|| " + commandId[0] + "||" + currentByte;
                        }
                    }
                    break;
                case 3:
                    if (commandId != null) {
                        if (currentByte != commandId[1]) {
                            receiveModel.isFinish = true;
                            receiveModel.isSuccess = false;
                            receiveModel.errorMsg = "Note:Wrong CommandId Low Byte|| " + commandId[1] + "||" + currentByte;
                        }
                    }
                    break;
                case 4:
                    if (currentByte != 0x00) {
                        receiveModel.isFinish = true;
                        receiveModel.isSuccess = false;
                        receiveModel.errorMsg = "Note:Invalid Command|| 0x00||" + currentByte;
                    }
                    break;
                case 5:
                    dataHeadIndex = currentIndex;
                    break;
                default:
                    break;
            }

        }
        return receiveModel;
    }

    public int GetDataHeadIndex() {
        return dataHeadIndex;
    }

    public int GetDataLength() {
        return super.GetDataLength() - 5;
    }

    public CommandResponseChecker(int headIndex) {
        super(headIndex);
    }

    public IFrameChecker CreateInstance(int HeadIndex) {
        return new CommandResponseChecker(HeadIndex);
    }

}
