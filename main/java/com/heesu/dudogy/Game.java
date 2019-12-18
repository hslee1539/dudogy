package com.heesu.dudogy;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

public class Game extends Handler {
    private ScoreRecyclerAdapter scoreRecyclerAdapter;
    private float box[];
    private Button objs[];
    private boolean objs_visible[];
    private final int data_initTime = 30;
    private int data_time = 30;
    private final int data_initScore = 0;
    private int data_score = 0;
    private TextView score;
    private TextView time;
    private RelativeLayout gameObjsLayout;
    private int inHeight;
    private int inWidth;
    private Screen screen;

    // 회전된 카메라 좌표계에서 안드로이드 좌표계로 변환하기 위한 변수
    private float k = 0;
    private float aw = 0;
    private float ah = 0;
    public Game(RelativeLayout gameObjsLayout, Button objs[], Screen screen, TextView score, TextView time, ScoreRecyclerAdapter scoreRecyclerAdapter){
        super();
        this.scoreRecyclerAdapter = scoreRecyclerAdapter;
        this.gameObjsLayout = gameObjsLayout;
        this.screen = screen;
        this.objs = objs;
        this.time = time;
        this.score = score;
        this.objs_visible = new boolean[objs.length];

        int index = 0;
        // 여기서 이거 하는 것은 게임 관련된 동작이기에 여기서 클릭 이벤트 작성
        for(Button obj : objs){
            final int i = index;
            this.objs_visible[index] = false;
            obj.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    data_score++;
                    objs_visible[i] = false;
                }
            });
            index++;
        }
    }
    public void initGame(){
        data_time = data_initTime;
        data_score = data_initScore;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Random random = new Random(System.currentTimeMillis());
                int s = 0;
                while(screen.getId() == Screen.ScreenID.game && (data_time > 0)){
                    try {
                        // 두더지 랜덤 표시
                        int debugIndex = random.nextInt(objs_visible.length);
                        objs_visible[debugIndex] ^= true;
                        Thread.sleep(200);
                        s = (s + 1) % 5;
                        if(s == 0){
                            data_time--;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if(screen.getId() == Screen.ScreenID.game){
                    scoreRecyclerAdapter.pull(Integer.toString(data_score));
                    screen.setId(Screen.ScreenID.score);
                }
            }
        }).start();
    }

    public void setBox(float box[]){
        this.box = box;
    }

    @Override
    public void handleMessage(Message msg){
        super.handleMessage(msg);
        int outWidth = this.gameObjsLayout.getWidth();
        int outHeight = this.gameObjsLayout.getHeight();
        // 회전된 cv 카메라 좌표계에서 안드로이드 좌표계로 변환을 위한 상수
        float k = (outHeight * inHeight / outWidth - inWidth) / 2;
        float weightH = outWidth / inHeight;
        float weightW = outHeight / (k * 2 + inWidth);


        int index = 0;
        time.setText(Integer.toString(this.data_time));
        score.setText(Integer.toString(this.data_score));
        for(Button obj : objs){
            if(objs_visible[index]) {
                obj.setVisibility(View.VISIBLE);
            }
            else {
                obj.setVisibility(View.GONE);
            }
            //obj.setX(-1000);
            obj.setX((inHeight - this.box[3 + index * 4]) * weightH);
            //obj.setX(outWidth * (1 - this.box[3 + index * 4] / this.inWidth));
            obj.setY((k + this.box[0 + index * 4]) * weightW);
            //obj.setY(10);
            ViewGroup.LayoutParams lp = obj.getLayoutParams();
            // inWidth, inHeight는 회전 된 것을 고려 해야 함.
            lp.width = (int) ((this.box[3 + index * 4] - this.box[1 + index * 4]) * weightH);
            lp.height = (int)((this.box[2 + index * 4] - this.box[index * 4]) * weightW);
            obj.setLayoutParams(lp);
            obj.invalidate();
            index ++;
        }
    }

    public void setInputSize(int height, int width) {
        this.inHeight = height;
        this.inWidth = width;
    }
}
