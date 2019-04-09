package cn.hexing;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author caibinglong
 *         date 2019/3/19.
 *         desc 海兴电表  表型
 */

public class MeterType {
    public static final int IRAQ_SINGLE = 0x1011; //伊拉克 单相 定制
    public static final int IRAQ_ThreePhase = 0x3109;//伊拉克 三相 定制
    public static final int HZ_METER = 0x03; //杭州表
    public static final int SZ_METER = 0x04;//深圳表

    @IntDef({IRAQ_SINGLE, IRAQ_ThreePhase})
    @Retention(RetentionPolicy.SOURCE)
    public @interface MTypes {
    }
}
