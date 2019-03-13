package cn.hexing;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author caibinglong
 *         date 2019/3/13.
 *         desc 握手帧类型
 */

public class HexHandType {
    public static final int IRAQ = 1;//伊拉克定制协议
    public static final int HEXING = 2;//海兴协议

    @IntDef({IRAQ, HEXING})
    @Retention(RetentionPolicy.SOURCE)
    public @interface HandTypes {
    }
}
