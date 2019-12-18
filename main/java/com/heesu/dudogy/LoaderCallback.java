package com.heesu.dudogy;

import android.content.Context;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;

public class LoaderCallback extends BaseLoaderCallback {
    CameraBridgeViewBase cameraBridgeView;
    public LoaderCallback(Context AppContext, CameraBridgeViewBase cameraBridgeView) {
        super(AppContext);
        this.cameraBridgeView = cameraBridgeView;
    }

    @Override
    public void onManagerConnected(int status){
        switch (status){
            case LoaderCallbackInterface
                    .SUCCESS: {
                this.cameraBridgeView.enableView();
            } break;
            default: {
                super.onManagerConnected(status);
            } break;
        }
    }
}
