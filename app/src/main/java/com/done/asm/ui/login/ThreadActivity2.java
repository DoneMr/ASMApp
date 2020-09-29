package com.done.asm.ui.login;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.done.asm.Cost;
import com.done.asm.R;

public class ThreadActivity2 extends Activity {

    private TextView mTv1, mTv2;

    @Override
    @Cost
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread2);
        mTv1 = findViewById(R.id.tv_1);
        mTv2 = findViewById(R.id.tv_2);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}