package com.tsitsing.translation;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.tsitsing.translation.customEffect.LinearLayoutManagerWithScrollTop;
import com.tsitsing.translation.customView.LearnedAdapter;
import com.tsitsing.translation.emun.RequestType;
import com.tsitsing.translation.interfaces.SimpleCallBack;
import com.tsitsing.translation.utility.ReciteWordOptions;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

public class LearnedActivity extends AppCompatActivity {

    private Handler handler = new Handler();
    private MyApplication myApplication;
    private JSONArray jsonArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learned);

        myApplication = (MyApplication) getApplication();

        initRV();
    }

    private void initRV () {
        new Thread(){
            @Override
            public void run() {
                getLearned(new SimpleCallBack() {
                    @Override
                    public void onResponse(String string) {
                        try {
                            jsonArray = new JSONArray(string);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                RecyclerView recyclerView = findViewById(R.id.rv_recite);
                                LearnedAdapter learnedAdapter = new LearnedAdapter(getApplicationContext(), jsonArray);
                                LinearLayoutManager manager = new LinearLayoutManagerWithScrollTop(getApplicationContext());
                                recyclerView.setAdapter(learnedAdapter);
                                recyclerView.setLayoutManager(manager);
                                learnedAdapter.setOnItemClickListener(new LearnedAdapter.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(View view, int position) {

                                    }
                                    @Override
                                    public void onItemLongClick(View view, int position, final String word, final String plan) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(LearnedActivity.this);
                                        builder.setTitle(R.string.title_delete)
                                                .setMessage("删除此已背的单词？")
                                                .setPositiveButton(R.string.title_delete, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        new Thread(){
                                                            @Override
                                                            public void run() {
                                                                deleteLearned(plan, word, new SimpleCallBack() {
                                                                    @Override
                                                                    public void onResponse(String string) {
                                                                        handler.post(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                initRV();
                                                                                Toast.makeText(getApplicationContext(), R.string.toast_delete_success, Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        });
                                                                    }
                                                                });
                                                            }
                                                        }.start();
                                                    }
                                                })
                                                .setNegativeButton(R.string.cancel, null)
                                                .show();
                                    }
                                });
                            }
                        });
                    }
                });
            }
        }.start();
    }

    private void getLearned (final SimpleCallBack callBack) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String TAG = "getLearned";
        final String CHECK_HOST = "http://chieching.cn/TranslateServer/Learned";
        queue.cancelAll(TAG);
        StringRequest request = new StringRequest(Request.Method.POST, CHECK_HOST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callBack.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userName", myApplication.getUserName());
                params.put("requestType", RequestType.QUERY.toString());
                return params;
            }
        };
        request.setTag(TAG);
        queue.add(request);
    }

    //delete
    private void deleteLearned (final String planName, final String word, final SimpleCallBack callBack) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String TAG = "deleteLearned";
        final String CHECK_HOST = "http://chieching.cn/TranslateServer/Learned";
        queue.cancelAll(TAG);
        StringRequest request = new StringRequest(Request.Method.POST, CHECK_HOST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callBack.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("word", word);
                params.put("planName", planName);
                params.put("userName", myApplication.getUserName());
                params.put("requestType", RequestType.DELETE.toString());
                return params;
            }
        };
        request.setTag(TAG);
        queue.add(request);
    }
}
