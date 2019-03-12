package cn.hexing.dlt645.zigbee;

import cn.hexing.dlt645.check.IFrameChecker;
import cn.hexing.dlt645.model.ReceiveModel;

/**
 * @author caibinglong
 *         date 2018/12/11.
 *         desc desc
 */

public class TransmitResponseChecker extends ZigBeeFrameChecker {
    private byte[] shortAddress;

    public TransmitResponseChecker(int headIndex) {
        super(headIndex);
    }

    public void SetShortAddress(byte highByte, byte lowByte) {
        shortAddress = new byte[2];
        shortAddress[0] = highByte;
        shortAddress[1] = lowByte;
    }

    @Override
    public ReceiveModel Check(int currentIndex, byte currentByte) {
        ReceiveModel model = super.Check(currentIndex, currentByte);
        if (!model.isFinish) {
            int offset = currentIndex - super.GetHeadIndex() - 3;
            switch (offset) {
                case -2:
                    if (currentByte != 0x00) {
                        model.isFinish = true;
                        model.isSuccess = false;
                        model.errorMsg = "Note:Wrong Frame Length HighByte =0x00||" + currentByte;
                        return model;
                    }
                    break;
                case -1:
                    if (currentByte != 0x07) {
                        model.isFinish = true;
                        model.isSuccess = false;
                        model.errorMsg = "Note:Wrong Frame Length LowByte=0x07||" + currentByte;
                        return model;
                    }
                    break;
                case 0:
                    if (currentByte != (byte) 0x8B) {
                        model.isFinish = true;
                        model.isSuccess = false;
                        model.errorMsg = "Note:Wrong Frame Type =0x8B||" + currentByte;
                        return model;
                    }
                    break;
                case 1:
                    if (currentByte != 0x01) {
                        model.isFinish = true;
                        model.isSuccess = false;
                        model.errorMsg = "Note:Wrong Frame Id = 0x01 ||" + currentByte;
                        return model;
                    }
                    break;
                case 2:
                    if (shortAddress != null) {
                        if (currentByte != shortAddress[0]) {
                            model.isFinish = true;
                            model.isSuccess = false;
                            model.errorMsg = "Note:Wrong shortAddress High Byte=" + shortAddress[0] + "||" + currentByte;
                            ;
                            return model;
                        }
                    }
                    break;
                case 3:
                    if (shortAddress != null) {
                        if (currentByte != shortAddress[1]) {
                            model.isFinish = true;
                            model.isSuccess = false;
                            model.errorMsg = "Note:Wrong shortAddress Low Byte=" + shortAddress[1] + "||" + currentByte;
                            return model;
                        }
                    }
                    break;
                case 5:
                    if (currentByte != 0x00) {
                        model.isFinish = true;
                        model.isSuccess = false;
                        model.errorMsg = "Note:Wrong Delivery Status 0x00=" + currentByte;
                        return model;
                    }
                    break;
                default:
                    break;
            }
        }
        return model;
    }

    @Override
    public IFrameChecker CreateInstance(int HeadIndex) {
        return new TransmitResponseChecker(HeadIndex);
    }

    @Override
    public int GetDataHeadIndex() {
        return super.GetDataHeadIndex() + 4;
    }

    @Override
    public int GetDataLength() {
        return 3;
    }

}
