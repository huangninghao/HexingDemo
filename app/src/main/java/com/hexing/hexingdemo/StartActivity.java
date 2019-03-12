package com.hexing.hexingdemo;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.hexing.hexingdemo.view.CustomVideoView;

/**
 * @author caibinglong
 *         date 2018/3/12.
 *         desc desc
 */

public class StartActivity extends AppCompatActivity implements View.OnClickListener {
    private CustomVideoView videoview;
    private Button btn_start;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        initView();
    }

    /**
     * 初始化
     */
    private void initView() {
        btn_start = (Button) findViewById(R.id.btn_start);
        btn_start.setOnClickListener(this);


        videoview = (CustomVideoView) findViewById(R.id.videoview);
        //设置播放加载路径
        videoview.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.xsyd));
        //播放
        videoview.start();


        //循环播放
        videoview.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                videoview.start();
            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_start:
                //Toast.makeText(this, "进入了主页", Toast.LENGTH_SHORT).show();
                videoview.stopPlayback();
                startActivity(new Intent(this, MainActivity.class));
                break;
        }
    }
}
