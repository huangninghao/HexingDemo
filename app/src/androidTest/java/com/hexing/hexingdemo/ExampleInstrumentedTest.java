package com.hexing.hexingdemo;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.hexing.libhexbase.tools.file.FileUtil;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.hexing.HexStringUtil;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        //Context appContext = InstrumentationRegistry.getTargetContext();

        //assertEquals("com.hexing.hexingdemo", appContext.getPackageName());

//        byte[] bytes = HexStringUtil.hexToByte("0c");
//        byte[] bytes1 = HexStringUtil.hexToByte("0C");
//        System.out.println(Arrays.toString(bytes) + "||" + Arrays.toString(bytes1));

    }


    public void testWriteByStringArray() throws Exception {

//        String[] record1 = {"1", "张三", "20", "1990-08-08"};
//        String[] record2 = {"2", "lisi", "21", "1991-08-08"};
//        String[] record3 = {"3", "wangwu", "22", "1992-08-08"};
//        List<String[]> allLines = new ArrayList<String[]>();
//        allLines.add(record1);
//        allLines.add(record2);
//        allLines.add(record3);
        //FileUtil.
    }
}
