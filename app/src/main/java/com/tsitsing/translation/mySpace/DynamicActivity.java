package com.tsitsing.translation.mySpace;

import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tsitsing.translation.MyApplication;
import com.tsitsing.translation.R;
import com.tsitsing.translation.interfaces.ActivityCallBack;
import com.tsitsing.translation.interfaces.SimpleCallBack;
import com.tsitsing.translation.utility.MySpaceOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class DynamicActivity extends AppCompatActivity {

    private Handler handler = new Handler();
    private MyApplication myApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic);

        myApplication = (MyApplication) getApplication();

        final MySpaceOptions mySpaceOptions = new MySpaceOptions(getApplicationContext(), myApplication.getUserName());
        new Thread(){
            @Override
            public void run() {
                mySpaceOptions.getUserInfo(new ActivityCallBack() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        Log.d("____userInfo object______", jsonObject.toString());
                        try {
                            String following = jsonObject.getString("following");
                            following = following.replace("[", "");
                            following = following.replace("]", "");
                            following = following.replace(" ", "");
                            List<String> fList;
                            fList = Arrays.asList(following.split(","));
                            for (String s : fList) {
                                mySpaceOptions.getDynamic(s, new SimpleCallBack() {
                                    @Override
                                    public void onResponse(final String string) {
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    JSONArray jsonArray = new JSONArray(string);
                                                    for (int i = 0; i < jsonArray.length(); i++) {
                                                        JSONObject object = jsonArray.getJSONObject(i);
                                                        LinearLayout container = findViewById(R.id.linearLayout_dyn_DList);
                                                        View view = View.inflate(getApplicationContext(), R.layout.dynamic_card, null);

//                                                        LinearLayout shell = new LinearLayout(getApplicationContext());
//                                                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
//                                                                LinearLayout.LayoutParams.MATCH_PARENT);
//                                                        params.setMargins(0, 20, 0, 20);
//                                                        shell.setLayoutParams(params);

                                                        TextView tvUserName = view.findViewById(R.id.tv_dynamicCar_userName);
                                                        tvUserName.setText(object.getString("userName"));
                                                        TextView tvContent = view.findViewById(R.id.tv_dynamicCar_content);
                                                        tvContent.setText(object.getString("content"));
                                                        TextView tvIssueDate = view.findViewById(R.id.tv_dynamicCar_date);
                                                        tvIssueDate.setText(object.getString("issueDate"));

//                                                        shell.addView(view);
                                                        container.addView(view);
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }.start();
    }
}
