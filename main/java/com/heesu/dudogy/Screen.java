package com.heesu.dudogy;

import android.app.Activity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class Screen implements Runnable{
    private ScreenID id;
    private AppCompatActivity activity;
    private LinearLayout menuLayout;
    private RelativeLayout game_objLayout;
    private LinearLayout gameLayout;
    private LinearLayout scoreLayout;
    private int menuVisiblity;
    private int gameVisiblity;
    private int scoreVisiblity;

    public ScreenID getId() {
        return id;
    }

    public void setId(ScreenID id) {
        this.id = id;
        switch(this.id){
            case menu:
                this.menuVisiblity = View.VISIBLE;
                this.gameVisiblity = View.GONE;
                this.scoreVisiblity = View.GONE;
                break;
            case game:
                this.menuVisiblity = View.GONE;
                this.gameVisiblity = View.VISIBLE;
                this.scoreVisiblity = View.GONE;
                break;
            case score:
                this.menuVisiblity = View.GONE;
                this.gameVisiblity = View.GONE;
                this.scoreVisiblity = View.VISIBLE;
                break;
        }
        activity.runOnUiThread(this);
    }

    public Screen(AppCompatActivity activity, LinearLayout menuLayout, RelativeLayout game_objLayout,LinearLayout gameLayout, LinearLayout scoreLayout){
        this.activity = activity;
        this.menuLayout = menuLayout;
        this.game_objLayout = game_objLayout;
        this.gameLayout = gameLayout;
        this.scoreLayout = scoreLayout;
    }

    @Override
    public void run() {
        this.menuLayout.setVisibility(this.menuVisiblity);
        this.game_objLayout.setVisibility(this.gameVisiblity);
        this.gameLayout.setVisibility(this.gameVisiblity);
        this.scoreLayout.setVisibility(this.scoreVisiblity);
    }

    public enum ScreenID {
        menu,
        game,
        score
    }
}
