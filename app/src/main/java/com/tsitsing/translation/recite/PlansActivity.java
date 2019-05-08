package com.tsitsing.translation.recite;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.tsitsing.translation.interfaces.BasicCallBack;
import com.tsitsing.translation.MyApplication;
import com.tsitsing.translation.R;
import com.tsitsing.translation.customView.CircleImageWithShadow;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PlansActivity extends AppCompatActivity {

    private int ADD_PLAN_REQUEST = 10;
    private String userName;
    private LinearLayout plansContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plans);

        MyApplication myApplication = (MyApplication) getApplication();
        userName = myApplication.getUserName();
        plansContainer = findViewById(R.id.ly_plans_container);

        //进入页面时显示已有计划
        displayPlans();

        //圆形添加计划按钮
        CircleImageWithShadow addPlan = findViewById(R.id.img_addPlan_add);
        addPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddPlanActivity.class);
                startActivityForResult(intent, ADD_PLAN_REQUEST);
            }
        });
    }

    //生成计划单元
    private LinearLayout generatePlanUnit (final String planName) {
        //计划单元layout
        LinearLayout linearLayout = new LinearLayout(getApplicationContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        params.setMarginStart(36);
        params.setMarginEnd(36);
        linearLayout.setLayoutParams(params);
        //显示计划名的view
        TextView textView = new TextView(getApplicationContext());
        textView.setText(planName.toUpperCase());
        textView.setIncludeFontPadding(false);
        textView.setGravity(Gravity.CENTER);
        ImageView imageView = new ImageView(getApplicationContext());
        imageView.setBackground(getApplicationContext().getResources().getDrawable(R.mipmap.dict_q, null));
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),DetailActivity.class);
                intent.putExtra("planName", planName);
                //跳转至背词界面
                startActivity(intent);
            }
        });
        linearLayout.addView(imageView);
        linearLayout.addView(textView);
        return linearLayout;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == ADD_PLAN_REQUEST) {
                displayPlans();
            }
        }
    }

    //显示已有计划的方法
    private void displayPlans () {
        getAllPlans(userName, new BasicCallBack() {
            @Override
            public void doSuccess() {

            }

            @Override
            public void doSuccess(String string) {
                List<String> list;
                String listStr;
                listStr = string.replace("[", "");
                listStr = listStr.replace("]", "");
                listStr = listStr.replace(" ", "");
                list = Arrays.asList(listStr.split(","));
                if (!(list.toString().equals("[]"))) {
                    plansContainer.removeAllViews();
                    for (int i = 0; i <list.size(); i++) {
                        LinearLayout layout = generatePlanUnit(list.get(i));
                        plansContainer.addView(layout);
                    }
                }
            }

            @Override
            public void doFail() {

            }
        });
    }

    //查询所有已学计划
    private void getAllPlans (final String userName, final BasicCallBack callBack) {
        final String PLAN_HOST = "http://chieching.cn/TranslateServer/Existence";
        final String TAG = "getAllPlans";
        final RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.cancelAll(TAG);
        StringRequest getPlansRequest = new StringRequest(Request.Method.POST, PLAN_HOST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callBack.doSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userName", userName);
                return params;
            }
        };
        getPlansRequest.setTag(TAG);
        requestQueue.add(getPlansRequest);
    }
}
