package com.tsitsing.translation;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.tsitsing.translation.interfaces.SimpleCallBack;
import com.tsitsing.translation.utility.ReciteWordOptions;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        MyApplication myApplication = (MyApplication) getApplication();
        String userName = myApplication.getUserName();
        String planName = "toefl";
    }
}
