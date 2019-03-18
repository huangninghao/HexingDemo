package com.hexing.libhexbase.log;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by caibinglong
 * on 15/11/18.
 */
public class HexJsonLog {

    /**
     * 只能 通过 HexLog 调用
     * @param tag tag
     * @param msg 内容
     * @param headString 头字符串
     */
    protected static void printJson(String tag, String msg, String headString) {

        String message;

        try {
            if (msg.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(msg);
                message = jsonObject.toString(HexLog.JSON_INDENT);
            } else if (msg.startsWith("[")) {
                JSONArray jsonArray = new JSONArray(msg);
                message = jsonArray.toString(HexLog.JSON_INDENT);
            } else {
                message = msg;
            }
        } catch (JSONException e) {
            message = msg;
        }

        HexBaseLog.printLine(tag, true);
        message = headString + HexLog.LINE_SEPARATOR + message;
        String[] lines = message.split(HexLog.LINE_SEPARATOR);
        for (String line : lines) {
            Log.d(tag, "║ " + line);
        }
        HexBaseLog.printLine(tag, false);
    }
}
