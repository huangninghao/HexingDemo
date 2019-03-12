package cn.hexing.dlt645;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author caibinglong
 *         date 2018/12/10.
 *         desc desc
 */

public class SendFrameTypes {
    public static final int ZigbeeTransmit = 0;
    public static final int ZigbeeATCommand = 1;
    public static final int ZigbeeRemoteATCommand = 2;
    public static final int ZigbeeNetworkSearchCommand = 3;//zigbee网络搜索
    public static final int C645Transmit = 4;
    public static final int C645ZigbeeTransmit = 5;

    @IntDef({ZigbeeTransmit, ZigbeeATCommand, ZigbeeRemoteATCommand, ZigbeeNetworkSearchCommand, C645Transmit, C645ZigbeeTransmit})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SFrameTypes {
    }
}
