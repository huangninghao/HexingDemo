package cn.hexing.dlt645.check;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author caibinglong
 *         date 2018/12/10.
 *         desc desc
 */

public class FrameCheckerFilterTypes {
    public static final int ZigbeeCommandResponse = 0;
    public static final int ZigbeeRemoteCommandResponse = 1;
    public static final int ZigbeeTransmitResponse = 2;
    public static final int ZigbeeReceivedData = 3;
    public static final int ZigbeeCommandResponsedStateReport = 4;
    public static final int ZigbeeTransmitResponsedReceivedData = 5;
    public static final int ZigbeeNetworkSearchResponse = 6;
    public static final int C645Frame = 7;
    public static final int C645ZigbeeReceivedData = 8;

    @IntDef({ZigbeeCommandResponse, ZigbeeRemoteCommandResponse, ZigbeeTransmitResponse, ZigbeeReceivedData,
            ZigbeeCommandResponsedStateReport, ZigbeeTransmitResponsedReceivedData,
            ZigbeeNetworkSearchResponse, C645Frame, C645ZigbeeReceivedData})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FCheckerFilterTypes {
    }
}
