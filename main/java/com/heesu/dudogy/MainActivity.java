package com.heesu.dudogy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.sql.Time;
import java.util.Date;
import java.util.Timer;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("opencv_java4");
        System.loadLibrary("native-lib");
    }

    private static final String TAG = "opencv";
    static final int PERMISSIONS_REQUEST_CODE = 1000;
    String[] PERMISSIONS = {"android.permission.CAMERA"};

    private LoaderCallback loaderCallback;
    private CameraBridgeViewBase cameraBridgeViewBase;
    private CvCameraViewListener cameraViewListener;
    private Screen screen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(!hasPermissions(PERMISSIONS)){
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        }

        LinearLayout menuLayout = findViewById(R.id.menuLayout);
        LinearLayout gameLayout = findViewById(R.id.gameLayout);
        LinearLayout scoreLayout = findViewById(R.id.scoreLayout);
        RelativeLayout game_objsLayout = findViewById(R.id.gameObjsLayout);


        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.menu, menuLayout, true);
        //inflater.inflate(R.layout.game_objs, game_objsLayout, true);
        inflater.inflate(R.layout.game, gameLayout, true);
        inflater.inflate(R.layout.score, scoreLayout, true);

        int maxNumber = 10;
        Button objs[] = new Button[maxNumber];
        for (int i = 0; i < maxNumber; i++){
            objs[i] = new Button(this);
            objs[i].setId(i + 100);
            objs[i].setX(-10);
            objs[i].setY(-10);
            objs[i].setBackgroundResource(R.drawable.dudogy);
            game_objsLayout.addView(objs[i], new RelativeLayout.LayoutParams(1,1));
        }

        this.screen = new Screen(this, menuLayout, game_objsLayout, gameLayout, scoreLayout);
        this.screen.setId(Screen.ScreenID.menu);

        ScoreRecyclerAdapter scoreRecyclerAdapter = new ScoreRecyclerAdapter();
        RecyclerView recyclerView = findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(scoreRecyclerAdapter);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration(){
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                if (parent.getChildAdapterPosition(view) != parent.getAdapter().getItemCount() - 1)
                    outRect.bottom = 25;
            }
        });
        scoreRecyclerAdapter.pull("33");

        final Game game = new Game(game_objsLayout, objs, screen, (TextView) findViewById(R.id.textView_score), (TextView)findViewById(R.id.textView_time), scoreRecyclerAdapter);

        findViewById(R.id.button_playGame).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                screen.setId(Screen.ScreenID.game);
                game.initGame();
            }
        });

        menuLayout.findViewById(R.id.button_viewScore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                screen.setId(Screen.ScreenID.score);
            }
        });

        gameLayout.findViewById(R.id.button_backMenu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                screen.setId(Screen.ScreenID.menu);
            }
        });
        scoreLayout.findViewById(R.id.button_scoreBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                screen.setId(Screen.ScreenID.menu);
            }
        });

        this.cameraViewListener = new CvCameraViewListener(this.screen, game, maxNumber);
        this.cameraBridgeViewBase = findViewById(R.id.activity_surface_view);
        this.cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        this.cameraBridgeViewBase.setCvCameraViewListener(this.cameraViewListener);
        this.cameraBridgeViewBase.setCameraIndex(0);
        this.loaderCallback = new LoaderCallback(this, this.cameraBridgeViewBase);
        this.loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        // Example of a call to a native method

    }

    @Override
    public void onPause(){
        super.onPause();
        if(this.cameraBridgeViewBase != null)
            this.cameraBridgeViewBase.disableView();
    }

    @Override
    public void onResume(){
        super.onResume();

        if(!OpenCVLoader.initDebug()){
            Log.d(TAG, "onResume :: Internal OpenCV library not found.");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, this.loaderCallback);
        }
        else{
            Log.d(TAG, "OnResume :: Opencv library found inside package. Using it!");
            this.loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(this.cameraBridgeViewBase != null){
            this.cameraBridgeViewBase.disableView();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case PERMISSIONS_REQUEST_CODE:
                if(grantResults.length > 0){
                    boolean cameraPermissionAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(!cameraPermissionAccepted)
                        showDialogForPermission("앱을 실행하려면 퍼미션을 허가하셔야 합니다.");
                }
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForPermission(String msg){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id){
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("아니요", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface arg0, int arg1){
                finish();
            }
        });
        builder.create().show();
    }

    private boolean hasPermissions(String[] permissions){
        int result;
        for (String perms : permissions){
            result = ContextCompat.checkSelfPermission(this, perms);
            if(result == PackageManager.PERMISSION_DENIED){
                //허가 안된 퍼미션
                return false;
            }
        }
        // 모두 허가 되어 있는 경우
        return true;
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
