package com.hexing.libhexbase.log;

import android.text.TextUtils;
import android.util.Log;


/**
 * Created by caibinglong
 * on 15/11/18.
 */
public class HexBaseLog {

    private static final int MAX_LENGTH = 4000;

    protected static void printDefault(int type, String tag, String msg) {

        int index = 0;
        int length = msg.length();
        int countOfSub = length / MAX_LENGTH;

        if (countOfSub > 0) {
            for (int i = 0; i < countOfSub; i++) {
                String sub = msg.substring(index, index + MAX_LENGTH);
                printSub(type, tag, sub);
                index += MAX_LENGTH;
            }
            printSub(type, tag, msg.substring(index, length));
        } else {
            printSub(type, tag, msg);
        }
    }

    private static void printSub(int type, String tag, String sub) {
        switch (type) {
            case HexLog.V:
                Log.v(tag, sub);
                break;
            case HexLog.D:
                Log.d(tag, sub);
                break;
            case HexLog.I:
                Log.i(tag, sub);
                break;
            case HexLog.W:
                Log.w(tag, sub);
                break;
            case HexLog.E:
                Log.e(tag, sub);
                break;
            case HexLog.A:
                Log.wtf(tag, sub);
                break;
        }
    }

    public static boolean isEmpty(String line) {
        return TextUtils.isEmpty(line) || line.equals("\n") || line.equals("\t") || TextUtils.isEmpty(line.trim());
    }

    public static void printLine(String tag, boolean isTop) {
        if (isTop) {
            Log.d(tag, "╔═══════════════════════════════════════════════════════════════════════════════════════");
        } else {
            Log.d(tag, "╚═══════════════════════════════════════════════════════════════════════════════════════");
        }
    }

}
