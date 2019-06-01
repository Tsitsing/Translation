package com.tsitsing.translation.recite;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.tsitsing.translation.MyApplication;
import com.tsitsing.translation.R;
import com.tsitsing.translation.customEffect.LinearLayoutManagerWithScrollTop;
import com.tsitsing.translation.customView.RecycleAdapter;
import com.tsitsing.translation.emun.PlanAPIType;
import com.tsitsing.translation.utility.GetDays;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddPlanActivity extends AppCompatActivity {

    LinearLayoutManagerWithScrollTop layoutManager;
    LinearLayoutManagerWithScrollTop layoutManagerDays;
    String planName = "cet4";
    String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_plan);

        MyApplication myApplication = (MyApplication) getApplication();
        userName = myApplication.getUserName();

        createPlanList(3849);

        //选择词典
        RadioGroup radioGroup = findViewById(R.id.dict_radio_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (group.getCheckedRadioButtonId()) {
                    case R.id.rb_addPlan_cet4 :
                        createPlanList(3849);
                        planName = "cet4";
                        break;
                    case R.id.rb_addPlan_cet6 :
                        createPlanList(5407);
                        planName = "cet6";
                        break;
                    case R.id.rb_addPlan_toefl :
                        createPlanList(6974);
                        planName = "toefl";
                        break;
                }
            }
        });

        //添加计划的按钮
        Button btnAddPlan = findViewById(R.id.btn_addPlan_add);
        btnAddPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int wordsPosition = layoutManager.findFirstVisibleItemPosition();
                int daysPosition = layoutManagerDays.findFirstVisibleItemPosition();//获取位置
                //获取recyclerView的组件
                ConstraintLayout wordsView =(ConstraintLayout) layoutManager.findViewByPosition(wordsPosition + 3);
                TextView wordsTV = (TextView) wordsView.getViewById(R.id.tv_item);
                int wordsNum = Integer.parseInt(wordsTV.getText().toString());

                ConstraintLayout daysView =(ConstraintLayout) layoutManagerDays.findViewByPosition(daysPosition + 3);
                TextView daysTV = (TextView) daysView.getViewById(R.id.tv_item);
                int daysNum = Integer.parseInt(daysTV.getText().toString());
                //添加计划
                checkPlan(userName, planName, PlanAPIType.GET_INFO, wordsNum);

                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });
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

    //创建两个相关联的recyclerView
    private void createPlanList (int wordsQuantity) {
        RecycleAdapter recycleAdapter = new RecycleAdapter(getApplicationContext(), getData());
        final RecyclerView recyclerView = findViewById(R.id.recyclerView);
        //设置适配器，添加数据
        recyclerView.setAdapter(recycleAdapter);
        layoutManager = new LinearLayoutManagerWithScrollTop(this);
        //必须进行此设置，指定显示效果
        recyclerView.setLayoutManager(layoutManager);

        RecycleAdapter recycleAdapterDays = new RecycleAdapter(getApplicationContext(), GetDays.getDays(wordsQuantity));
        final RecyclerView recyclerViewDays = findViewById(R.id.recyclerView_days);
        //设置适配器，添加数据
        recyclerViewDays.setAdapter(recycleAdapterDays);
        layoutManagerDays = new LinearLayoutManagerWithScrollTop(this);
        //必须进行此设置，指定显示效果
        recyclerViewDays.setLayoutManager(layoutManagerDays);

//        不起作用
//        recyclerView.smoothScrollToPosition(0);
//        recyclerViewDays.smoothScrollToPosition(13);

        recyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                int i = layoutManager.findFirstVisibleItemPosition();
                recyclerViewDays.smoothScrollToPosition(13 - i);
            }
        });

        recyclerViewDays.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                int i = layoutManagerDays.findFirstVisibleItemPosition();
                recyclerView.smoothScrollToPosition(13 - i);
            }
        });
    }

    private List<String> getData () {
        List<String> list = new ArrayList<>();
        list.add("");
        list.add("");
        list.add("");
        list.add("5");
        list.add("10");
        list.add("15");
        list.add("20");
        list.add("25");
        list.add("30");
        list.add("40");
        list.add("50");
        list.add("60");
        list.add("70");
        list.add("80");
        list.add("100");
        list.add("150");
        list.add("200");
        list.add("");
        list.add("");
        list.add("");
        return list;
    }
}
