package com.tsitsing.translation;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.tsitsing.translation.emun.PlanName;
import com.tsitsing.translation.recite.DetailActivity;

public class ReciteActivity extends AppCompatActivity {
    LinearLayout layoutCet6;
    Button btnAddPlan;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recite);
        //点击背六级词汇跳转页面
        layoutCet6 = findViewById(R.id.linear_cet6);
        layoutCet6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
                intent.putExtra("planName", PlanName.cet6.toString());
                startActivity(intent);
            }
        });
        //点击添加计划
        btnAddPlan = findViewById(R.id.btn_addPlan);
        btnAddPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
