package cn.hexing.dlt645.c645;

import cn.hexing.dlt645.check.IFrameChecker;
import cn.hexing.dlt645.model.ReceiveModel;
import cn.hexing.dlt645.zigbee.*;

/**
 * @author caibinglong
 *         date 2018/12/10.
 *         desc desc
 */

public class C645ZigbeeFrameChecker extends ZigBeeReceiveChecker {
    public C645FrameChecker C645Checker;

    public void SetFrameCheckerParameter(int type, byte[] parameter) {
        super.SetFrameCheckerParameter(type, parameter);
        if (C645Checker != null) {
            C645Checker.SetFrameCheckerParameter(type, parameter);
        }
    }

    public byte[] GetReceivedFrameParameter(int type) {
        if (super.GetReceivedFrameParameter(type) == null || super.GetReceivedFrameParameter(type).length == 0) {
            return C645Checker.GetReceivedFrameParameter(type);
        } else {
            return super.GetReceivedFrameParameter(type);
        }
    }

    public C645ZigbeeFrameChecker(int headIndex) {
        super(headIndex);
    }

    public ReceiveModel Check(int currentIndex, byte currentByte) {
        ReceiveModel model = super.Check(currentIndex, currentByte);
        if (!model.isFinish) {
            int offset = currentIndex - super.GetDataHeadIndex();
            if (offset == 0) {
                C645Checker = new C645FrameChecker(currentByte);
                if (currentByte == C645Checker.GetHeadByte()) {
                    C645Checker = new C645FrameChecker(currentIndex);
                } else {
                    model.isFinish = true;
                    model.isSuccess = false;
                    model.errorMsg = "Note:Wrong 645 head =" + C645Checker.GetHeadByte() + "||" + currentByte;
                }
            } else if (offset > 0) {
                model = C645Checker.Check(currentIndex, currentByte);
            }
        }
        return model;
    }

    public int GetHeadIndex() {
        return super.GetHeadIndex();
    }

    public int GetDataHeadIndex() {
        if (C645Checker != null) {
            return C645Checker.GetDataHeadIndex();
        } else {
            return -1;
        }
    }

    public int GetDataLength() {
        if (C645Checker != null) {
            return C645Checker.GetDataLength();
        } else {
            return -1;
        }
    }

    public byte GetHeadByte() {
        return super.GetHeadByte();
    }

    public IFrameChecker CreateInstance(int HeadIndex) {
        return new C645ZigbeeFrameChecker(HeadIndex);
    }

}
