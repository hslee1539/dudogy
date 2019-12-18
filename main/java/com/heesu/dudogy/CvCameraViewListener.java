package com.heesu.dudogy;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import static org.opencv.imgproc.Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C;
import static org.opencv.imgproc.Imgproc.ADAPTIVE_THRESH_MEAN_C;
import static org.opencv.imgproc.Imgproc.COLOR_GRAY2RGBA;
import static org.opencv.imgproc.Imgproc.COLOR_RGB2GRAY;
import static org.opencv.imgproc.Imgproc.COLOR_RGBA2GRAY;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.THRESH_OTSU;
import static org.opencv.imgproc.Imgproc.adaptiveThreshold;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.filter2D;

public class CvCameraViewListener implements CameraBridgeViewBase.CvCameraViewListener2, Runnable {
    private Mat inputFrame = null;
    private Mat outputFrame = null;
    private Mat box = null;
    private float outBox[];
    private Screen screen;
    private Game game;

    // 회전된 카메라 좌표계에서 안드로이드 좌표계로 변환하기 위한 변수
    private float k = 0;
    private float aw = 0;
    private float ah = 0;

    public CvCameraViewListener(Screen screen, Game game, int maxNumber){
        this.game = game;
        this.box = Mat.zeros(maxNumber, 2, CvType.CV_32FC2);
        this.outBox = new float[maxNumber * 2 * 2];
        this.screen = screen;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        this.game.setInputSize(height, width);
    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        this.inputFrame = inputFrame.rgba();
        if(this.outputFrame == null){
            this.outputFrame = new Mat(this.inputFrame.rows(), this.inputFrame.cols(),
                    this.inputFrame.type());
        }
        switch(screen.getId()){
            case menu:{
                Imgproc.medianBlur(this.inputFrame, this.outputFrame, 9);
            } break;
            case game:{
                // 물체 인식을 비동기로 실행
                //Thread thread = new Thread(this);
                //thread.start();
                    //try {
                    // 없으면 쓰래드 엄청 생겨서 강제 종료 됨
                    //thread.join(500);
                //} catch (InterruptedException e){
                    //e.printStackTrace();
                //}
                this.run();
                //Mat tmp = new Mat();
                //Mat tmp2 = new Mat();
                //cvtColor(this.inputFrame, tmp, COLOR_RGBA2GRAY);
                //adaptiveThreshold(tmp, tmp2, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY + THRESH_OTSU, 31, -20);
                //cvtColor(tmp2, this.outputFrame, COLOR_GRAY2RGBA);
                this.inputFrame.copyTo(this.outputFrame);
            } break;
            case score:{
                Imgproc.medianBlur(this.inputFrame, this.outputFrame, 7);
            } break;
        }

        return this.outputFrame;
    }

    @Override
    public synchronized void run() {
        // game 처리 부분
        //findRectangle(this.inputFrame.getNativeObjAddr(), this.box.getNativeObjAddr());
        findRectangle(this.inputFrame.getNativeObjAddr(), this.box.getNativeObjAddr());
        this.box.get(0,0,this.outBox);
        this.game.setBox(this.outBox);
        this.game.sendEmptyMessage(0);
    }

    public native void findRectangle(long inputAddress, long boxAddress);
}
