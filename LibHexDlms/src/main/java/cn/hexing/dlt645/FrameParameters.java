package cn.hexing.dlt645;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author caibinglong
 *         date 2018/12/7.
 *         desc desc
 */

public class FrameParameters {
    public static final int ZigbeeLongAddress = 0;
    public static final int ZigbeeShortAddress = 1;
    public static final int ZigbeeCommandId = 2;
    public static final int C645Address = 3;
    public static final int C645ControlCode = 4;
    public static final int ZigbeeNetWork = 5;//新增zigbee网络查询--2012-8-30

    @IntDef({ZigbeeLongAddress, ZigbeeShortAddress, ZigbeeCommandId, C645Address, C645ControlCode, ZigbeeNetWork})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FParameters {
    }
}
