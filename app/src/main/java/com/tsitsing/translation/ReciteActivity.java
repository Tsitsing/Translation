package com.tsitsing.translation;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.tsitsing.translation.emun.PlanAPIType;
import com.tsitsing.translation.emun.PlanName;
import com.tsitsing.translation.emun.ResponseState;
import com.tsitsing.translation.interfaces.ActivityCall;
import com.tsitsing.translation.recite.DetailActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ReciteActivity extends AppCompatActivity {
    private TextView planView;
    private TextView lAndTView;
    private Button deleteBtn;
    private LinearLayout shellLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recite);

        MyApplication application = (MyApplication) getApplication();
        final String userName = application.getUserName();

        final LinearLayout layout = findViewById(R.id.planContainer);

        //显示计划信息
        displayPlan(userName, new ActivityCall() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.d("_____json_____", jsonObject.toString());
                try {
                    if (!jsonObject.getString("planName").equals("")) {
                        planView = new TextView(getBaseContext());
                        planView.setWidth(360);
                        planView.setGravity(Gravity.CENTER_HORIZONTAL);
                        lAndTView = new TextView(getBaseContext());
                        lAndTView.setWidth(360);
                        lAndTView.setGravity(Gravity.CENTER_HORIZONTAL);
                        deleteBtn = new Button(getBaseContext());
                        deleteBtn.setWidth(360);
                        deleteBtn.setBackground(getDrawable(R.mipmap.delete));
                        deleteBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                checkPlan(userName, planView.getText().toString(), PlanAPIType.DELETE_PLAN);
                                restartActivity(ReciteActivity.this);
                            }
                        });
                        shellLayout = new LinearLayout(getBaseContext());
                        planView.setText(jsonObject.getString("planName"));
                        //TODO:还要查询出总词数
                        String detail = jsonObject.getString("list").replace("[", "");
                        detail = detail.replace("]", "");
                        ArrayList<String> list = new ArrayList<>(Arrays.asList(detail.split(",")));
                        Log.d("_____list content_____", list.toString());
                        int i;
                        if (list.get(0).isEmpty()) {
                            i = 0;
                        } else {
                            i = list.size();
                        }
                        Log.d("_____list size_____", String.valueOf(i));
                        lAndTView.setText(String.valueOf(i));
                        shellLayout.addView(planView);
                        shellLayout.addView(lAndTView);
                        shellLayout.addView(deleteBtn);
                        layout.addView(shellLayout);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        //点击背六级词汇跳转页面
        LinearLayout layoutCet6 = findViewById(R.id.linear_cet6);
        layoutCet6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String planName = PlanName.cet6.toString();
                checkPlan(userName, planName, PlanAPIType.GET_INFO);
                Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
                intent.putExtra("planName", planName);
                startActivity(intent);
            }
        });
        //点击背四级词汇跳转页面
        LinearLayout layoutCet4 = findViewById(R.id.linear_cet4);
        layoutCet4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String planName = PlanName.cet4.toString();
                checkPlan(userName, planName, PlanAPIType.GET_INFO);
                Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
                intent.putExtra("planName", PlanName.cet4.toString());
                startActivity(intent);
            }
        });
        //点击背托福词汇跳转页面
        LinearLayout layoutToefl = findViewById(R.id.linear_toefl);
        layoutToefl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String planName = PlanName.toefl.toString();
                checkPlan(userName, planName, PlanAPIType.GET_INFO);
                Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
                intent.putExtra("planName", PlanName.toefl.toString());
                startActivity(intent);
            }
        });
    }

    //点击删除计划后需要调用此方法从新加载Activity刷新页面
    public static void restartActivity(Activity activity){
        Intent intent = new Intent();
        intent.setClass(activity, activity.getClass());
        activity.startActivity(intent);
        activity.overridePendingTransition(0,0);
        activity.finish();
    }

    //对计划进行获取、添加、删除操作
    void checkPlan(final String userName, final String planName, final PlanAPIType type) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String TAG = "checkPlan";
        final String CHECK_HOST = "http://chieching.cn/TranslateServer/Plan";
        queue.cancelAll(TAG);
        StringRequest request = new StringRequest(Request.Method.POST, CHECK_HOST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.equals("{}")) {
                    checkPlan(userName, planName, PlanAPIType.ADD_PLAN);
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
                return params;
            }
        };
        request.setTag(TAG);
        queue.add(request);
    }

    //获取各个计划已学信息并显示
    void displayPlan(final String userName, final ActivityCall call) {
        final String GET_PLAN_HOST = "http://chieching.cn/TranslateServer/Existence";
        final String TAG = "getPlan";
        final String TAG_QUANTITY = "quantity";
        final RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.cancelAll(TAG);
        queue.cancelAll(TAG_QUANTITY);
        final StringRequest request = new StringRequest(Request.Method.POST, GET_PLAN_HOST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                String string = response.replace("[", "");
                string = string.replace("]", "");
                ArrayList<String> list = new ArrayList<String>(Arrays.asList(string.split(",")));
                if (list.size() != 0) {
                    for (int i = 0; i < list.size(); i++) {
                        final String planName = list.get(i).replace(" ", "");
                        final String GET_QUANTITY_HOST = "http://chieching.cn/TranslateServer/PlanDetail";
                        StringRequest getQuantity = new StringRequest(Request.Method.POST, GET_QUANTITY_HOST, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("planName", planName);
                                    jsonObject.put("list", response);
                                    call.onResponse(jsonObject);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        }) {
                            protected Map<String, String> getParams() {
                                Map<String, String> params = new HashMap<>();
                                params.put("userName", userName);
                                params.put("planName", planName);
                                return params;
                            }
                        };
                        getQuantity.setTag(TAG_QUANTITY);
                        queue.add(getQuantity);
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userName", userName);
                return params;
            }
        };
        request.setTag(TAG);
        queue.add(request);
    }
}
