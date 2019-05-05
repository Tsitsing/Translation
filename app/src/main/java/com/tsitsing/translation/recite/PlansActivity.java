package com.tsitsing.translation.recite;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.tsitsing.translation.MyApplication;
import com.tsitsing.translation.R;
import com.tsitsing.translation.customView.CircleImage;
import com.tsitsing.translation.emun.PlanAPIType;

import java.util.HashMap;
import java.util.Map;

public class PlansActivity extends AppCompatActivity {

    private int ADD_PLAN_REQUEST = 10;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plans);

        MyApplication myApplication = (MyApplication) getApplication();
        userName = myApplication.getUserName();

        CircleImage addPlan = findViewById(R.id.img_addPlan_add);
        addPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddPlanActivity.class);
                startActivityForResult(intent, ADD_PLAN_REQUEST);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == ADD_PLAN_REQUEST) {
                String planName = data.getStringExtra("planName");
                int numPerDay = data.getIntExtra("wordsNum", 0);
                checkPlan(userName, planName, PlanAPIType.GET_INFO, numPerDay);

            }
        }
    }

    //对计划进行获取、添加、删除操作
    void checkPlan(final String userName, final String planName, final PlanAPIType type, final int numPerDay) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String TAG = "checkPlan";
        final String CHECK_HOST = "http://chieching.cn/TranslateServer/Plan";
        queue.cancelAll(TAG);
        StringRequest request = new StringRequest(Request.Method.POST, CHECK_HOST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.equals("{}")) {
                    checkPlan(userName, planName, PlanAPIType.ADD_PLAN, numPerDay);
                }
                Log.d("_____", response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("type", type.toString());
                params.put("userName", userName);
                params.put("planName", planName);
                params.put("numPerDay", String.valueOf(numPerDay));
                return params;
            }
        };
        request.setTag(TAG);
        queue.add(request);
    }
}
