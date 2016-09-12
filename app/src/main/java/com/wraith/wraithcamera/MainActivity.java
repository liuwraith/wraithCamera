package com.wraith.wraithcamera;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

    private WraithCameraView mCameraPreview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCameraPreview = (WraithCameraView) findViewById(R.id.camera_preview);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraPreview.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraPreview.pause();
    }

}
