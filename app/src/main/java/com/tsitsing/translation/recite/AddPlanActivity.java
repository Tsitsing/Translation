package com.tsitsing.translation.recite;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.tsitsing.translation.R;
import com.tsitsing.translation.customEffect.LinearLayoutManagerWithScrollTop;
import com.tsitsing.translation.customView.RecycleAdapter;
import com.tsitsing.translation.utility.GetDays;
import java.util.ArrayList;
import java.util.List;

public class AddPlanActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_plan);

        createPlanList(3849);

        ImageView cet4Image = findViewById(R.id.iv_addPlan_cet4);
        ImageView cet6Image = findViewById(R.id.iv_addPlan_cet6);
        ImageView toeflImage = findViewById(R.id.iv_addPlan_toefl);

        cet4Image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPlanList(3849);
            }
        });
        cet6Image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPlanList(5407);
            }
        });
        toeflImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPlanList(6974);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void createPlanList (int wordsQuantity) {
        RecycleAdapter recycleAdapter = new RecycleAdapter(getApplicationContext(), getData());
        final RecyclerView recyclerView = findViewById(R.id.recyclerView);
        final LinearLayoutManagerWithScrollTop layoutManager = new LinearLayoutManagerWithScrollTop(this);
        //设置适配器，添加数据
        recyclerView.setAdapter(recycleAdapter);
        //必须进行此设置，指定显示效果
        recyclerView.setLayoutManager(layoutManager);

        RecycleAdapter recycleAdapterDays = new RecycleAdapter(getApplicationContext(), GetDays.getDays(wordsQuantity));
        final RecyclerView recyclerViewDays = findViewById(R.id.recyclerView_days);
        //设置适配器，添加数据
        recyclerViewDays.setAdapter(recycleAdapterDays);
        final LinearLayoutManagerWithScrollTop layoutManagerDays = new LinearLayoutManagerWithScrollTop(this);
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
