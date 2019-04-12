package com.tsitsing.translation.recite;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.tsitsing.translation.MyApplication;
import com.tsitsing.translation.R;
import com.tsitsing.translation.interfaces.ActivityCall;

import org.json.JSONException;
import org.json.JSONObject;

public class DetailActivity extends AppCompatActivity {
    private TextView textViewWord;
    private TextView textViewPronunciation;
    private TextView textViewTranslation;
    private TextView textViewDefinition;
    private Button btnVoice;
    private Button btnUp;
    private Button btnDown;
    private Button btnLearned;
    private String userName, planName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        //绑定组件
        textViewWord = findViewById(R.id.textV_recite_word);
        textViewPronunciation = findViewById(R.id.textV_recite_pronunciation);
        textViewTranslation = findViewById(R.id.textV_recite_translation);
        textViewDefinition = findViewById(R.id.textV_recite_definition);
        btnVoice = findViewById(R.id.btn_reciteVoice);
        btnUp = findViewById(R.id.btn_reciteUpPage);
        btnDown = findViewById(R.id.btn_reciteDownPage);
        btnLearned = findViewById(R.id.btn_reciteLearned);
        //获取用户名，计划名
        MyApplication application = (MyApplication) getApplication();
        userName = application.getUserName();
        Intent intent = getIntent();
        planName = intent.getStringExtra("planName");
        //初始化页面信息
        SingleProcedure.getInstance(userName, planName).init(getApplicationContext(), new ActivityCall() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                textEvent(jsonObject);
            }
        });
        btnEvent();
    }
    //按钮事件
    void btnEvent () {
        //监听下一页点击事件
        btnDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SingleProcedure.getInstance(userName, planName).downPage(new ActivityCall() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        textEvent(jsonObject);
                    }
                }, getApplicationContext());
            }
        });
        //监听上一页事件
        btnUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SingleProcedure.getInstance(userName, planName).upPage(new ActivityCall() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        textEvent(jsonObject);
                    }
                }, getApplicationContext());
            }
        });
        //监听已学按钮事件
        btnLearned.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SingleProcedure.getInstance(userName, planName).learned(textViewWord.getText().toString(),
                        getApplicationContext(), new ActivityCall() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                //TODO:改变ui
                                Log.d("_____________", "操作成功");
                            }
                        });
            }
        });
    }
    //设置文本
    void textEvent (JSONObject jsonObject) {
        try {
            textViewWord.setText(jsonObject.getString("word"));
            textViewPronunciation.setText(jsonObject.getString("pronunciation"));
            textViewTranslation.setText(jsonObject.getString("translation"));
            textViewDefinition.setText(jsonObject.getString("definition"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
