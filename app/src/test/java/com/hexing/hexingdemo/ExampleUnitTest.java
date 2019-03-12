package com.hexing.hexingdemo;

import com.hexing.hexingdemo.model.DataModel;
import com.hexing.libhexbase.tools.XmlHelperUtil;
import com.hexing.libhexbase.tools.file.FileUtil;

import org.junit.Test;

import java.io.InputStream;


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        byte[] receiveData = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};

        //System.out.println("test||" + String.format("%02X%02X%02X", receiveData[4] & 0xff, receiveData[3] & 0xff, receiveData[2] & 0xff));
       // assertEquals(4, 2 + 2);

        //InputStream stream = FileUtil.openAssetsFile(App.getInstance(), "Obis.xml");
        //DataModel dataModel = XmlHelperUtil.getInstance().getObject(DataModel.class, stream, "DataModel");
    }
}