package com.lyldding.quicksideview;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

/**
 * @author lyldding
 */
public class MainActivity extends AppCompatActivity implements SideView.OnSelectedListener {
    private TextView mTextView;
    private SideView mSideView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = findViewById(R.id.text);
        mSideView = findViewById(R.id.right_side);
        mSideView.setOnSelectedListener(this);

        findViewById(R.id.test1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSideView.setInterpolator(new LinearInterpolator());
            }
        });

        findViewById(R.id.test2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSideView.setInterpolator(new BounceInterpolator());
                mSideView.setSelectColor(Color.BLUE);
            }
        });

        findViewById(R.id.test3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSideView.setInterpolator(new OvershootInterpolator());
            }
        });
    }

    @Override
    public void onSelected(int currentPosition, String s) {
        mTextView.setText(s);
    }
}
