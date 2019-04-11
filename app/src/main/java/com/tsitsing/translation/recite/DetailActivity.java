package com.tsitsing.translation.recite;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
    private JSONObject jsonObject = new JSONObject();

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
        String userName = application.getUserName();
        Intent intent = getIntent();
        String planName = intent.getStringExtra("planName");

        SingleProcedure.getInstance().init(userName, planName, getApplicationContext(), new ActivityCall() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.d("_______jsonObject detail", jsonObject.toString());
                try {
                    textViewWord.setText(jsonObject.getString("word"));
                    textViewPronunciation.setText(jsonObject.getString("pronunciation"));
                    textViewTranslation.setText(jsonObject.getString("translation"));
                    textViewDefinition.setText(jsonObject.getString("definition"));
                } catch (JSONException e) {
                    Log.d("____________", "detail activity");
                    e.printStackTrace();
                }
            }
        });
        btnEvent();
    }
    //按钮事件
    void btnEvent () {

    }
}
