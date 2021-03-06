package com.tsitsing.translation.recite;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.tsitsing.translation.MyApplication;
import com.tsitsing.translation.R;
import com.tsitsing.translation.interfaces.ActivityCallBack;
import com.tsitsing.translation.utility.ReciteWordOptions;

import org.json.JSONException;
import org.json.JSONObject;

public class DetailActivity extends AppCompatActivity {
    private TextView textViewWord;
    private TextView textViewPronunciation;
    private TextView textViewTranslation;
    private TextView textViewDefinition;
    private Button btnVoice;
    private Button btnLearned;
    private float passX = 0, curX = 0;
    private ReciteWordOptions reciteWordOptions;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        handler = new Handler();
        //绑定组件
        textViewWord = findViewById(R.id.textV_recite_word);
        textViewPronunciation = findViewById(R.id.textV_recite_pronunciation);
        textViewTranslation = findViewById(R.id.textV_recite_translation);
        textViewDefinition = findViewById(R.id.textV_recite_definition);
//        btnVoice = findViewById(R.id.btn_reciteVoice);
        btnLearned = findViewById(R.id.btn_reciteLearned);
        //获取用户名，计划名
        MyApplication application = (MyApplication) getApplication();
        String userName = application.getUserName();
        Intent intent = getIntent();
        String planName = intent.getStringExtra("planName");
        reciteWordOptions = new ReciteWordOptions(userName,
                planName, getApplicationContext());
        //初始化页面信息
        new Thread(){
            @Override
            public void run() {
                reciteWordOptions.init(new ActivityCallBack() {
                    @Override
                    public void onResponse(final JSONObject jsonObject) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                textEvent(jsonObject);
                            }
                        });
                    }
                });
            }
        }.start();
        //弹窗提示操作
        if (application.isFirstSignIn()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle(R.string.recite_title)
                    .setMessage(R.string.recite_message)
                    .setPositiveButton(R.string.yes, null);
            builder.show();
            application.setFirstSignIn(false);
        }
        //监听上下页
        btnEvent();
    }

    //按钮事件
    void btnEvent() {
        //获取layout设置左右滑动监听事件，实现上下翻页
        View layout = getWindow().getDecorView().findViewById(android.R.id.content);
        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        passX = event.getX();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        curX = event.getX();
                        break;
                    case MotionEvent.ACTION_UP:
                        if ((passX - curX) < 0 && Math.abs(passX - curX) > 10) {//向左滑
                            //在子线程中请求
                            new Thread(){
                                @Override
                                public void run() {
                                    reciteWordOptions.upPage(new ActivityCallBack() {
                                        @Override
                                        public void onResponse(final JSONObject jsonObject) {
                                            //通知UI更新
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    textEvent(jsonObject);
                                                    setBtnLearned(jsonObject);
                                                }
                                            });
                                            textEvent(jsonObject);
                                            setBtnLearned(jsonObject);
                                        }
                                    });
                                }
                            }.start();
                        }
                        if ((passX - curX) > 0 && Math.abs(passX - curX) > 10) {//向右滑
                            reciteWordOptions.downPage(new ActivityCallBack() {
                                @Override
                                public void onResponse(JSONObject jsonObject) {
                                    textEvent(jsonObject);
                                    setBtnLearned(jsonObject);
                                }
                            });
                        }
                        break;
                }
                return true;
            }
        });
        //监听已学按钮事件
        btnLearned.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reciteWordOptions.learnedAction(textViewWord.getText().toString(), new ActivityCallBack() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        setBtnLearned(jsonObject);
                    }
                });
            }
        });
    }

    //改变learned按钮背景状态
    void setBtnLearned(JSONObject jsonObject) {
        try {
            if (jsonObject.getString("isLearned").equals("true")) {
                //已学
                btnLearned.setBackground(getDrawable(R.mipmap.smile));
            } else {
                //未学
                btnLearned.setBackground(getDrawable(R.mipmap.pokerf));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //设置文本
    void textEvent(JSONObject jsonObject) {
        try {
            textViewWord.setText(jsonObject.getString("word"));
            textViewPronunciation.setText("[ " + jsonObject.getString("pronunciation") + " ]");
            textViewTranslation.setText(jsonObject.getString("translation"));
            textViewDefinition.setText(jsonObject.getString("definition"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
