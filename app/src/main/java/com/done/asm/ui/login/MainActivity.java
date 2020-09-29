package com.done.asm.ui.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.done.asm.R;
import com.done.asm.Utils;
import com.done.asm.ui.login.ui.main.MainFragment;

public class MainActivity extends AppCompatActivity {

    private static final int LOGIN_CODE = 1;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        mContext = this;
        initViews();
//        if (savedInstanceState == null) {
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.container, MainFragment.newInstance())
//                    .commitNow();
//        }
//        LoginViewModel loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
//                .get(LoginViewModel.class);
//        LiveData<LoginResult> loginResult = loginViewModel.getLoginResult();
//        LoginResult value = loginResult == null ? null : loginResult.getValue();
//        boolean dataValid = value != null && value.getSuccess() != null;
//        if (!dataValid) {
//            startActivityForResult(new Intent(MainActivity.this, LoginActivity.class), LOGIN_CODE);
//        }
    }

    private void initViews() {
        findViewById(R.id.btn_to_kt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, ThreadActivity.class));
            }
        });
        findViewById(R.id.btn_to_java).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, ThreadActivity2.class));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOGIN_CODE && resultCode == Activity.RESULT_OK) {
            Toast.makeText(getApplicationContext(), "登录成功", Toast.LENGTH_LONG).show();
        }
    }

    private void test() {
        long start = System.currentTimeMillis();
        Utils.printCost("test", start);
    }
}