package com.hexing.hexingdemo;

import com.cbl.dialog.StyledDialog;
import com.hexing.libhexbase.application.HexApplication;

/**
 * @author caibinglong
 *         date 2018/3/14.
 *         desc desc
 */

public class App extends HexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        StyledDialog.init(getApplicationContext());
        //Mapbox.getInstance(getApplicationContext(), getString(R.string.access_token));

    }

}
