package com.tsitsing.translation.recite;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.tsitsing.translation.R;
import com.tsitsing.translation.customView.CircleImage;

public class PlansActivity extends AppCompatActivity {

    private int ADD_PLAN_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plans);

        CircleImage addPlan = findViewById(R.id.img_addPlan_add);
        addPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddPlanActivity.class);
                startActivityForResult(intent, ADD_PLAN_REQUEST);
            }
        });
    }
}
