package com.tsitsing.translation;

import android.content.Context;
import android.content.DialogInterface;
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
import com.tsitsing.translation.customView.CollAdapter;
import com.tsitsing.translation.emun.PlanAPIType;
import com.tsitsing.translation.emun.RequestType;
import com.tsitsing.translation.interfaces.SimpleCallBack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CollectionActivity extends AppCompatActivity {

    private MyApplication myApplication;
    private Handler handler = new Handler();
    private JSONArray jsonArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);

        myApplication = (MyApplication) getApplication();


        configRV(this);
    }

    private void configRV(final Context context) {
        new Thread() {
            @Override
            public void run() {
                getCollection(myApplication.getUserName(), new SimpleCallBack() {
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
                                final RecyclerView recyclerView = findViewById(R.id.rv_collection);
                                final CollAdapter collAdapter = new CollAdapter(context, jsonArray);
                                LinearLayoutManager manager = new LinearLayoutManagerWithScrollTop(context);
                                recyclerView.setAdapter(collAdapter);
                                recyclerView.setLayoutManager(manager);
                                collAdapter.setOnItemClickListener(new CollAdapter.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(View view, int position) {

                                    }

                                    @Override
                                    public void onItemLongClick(View view, int position, final String contentSource) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(CollectionActivity.this);
                                        builder.setTitle(R.string.title_delete)
                                                .setMessage(R.string.message_coll_delete)
                                                .setPositiveButton(R.string.title_delete, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        new Thread(){
                                                            @Override
                                                            public void run() {
                                                                deleteCollection(myApplication.getUserName(), contentSource, new SimpleCallBack() {
                                                                    @Override
                                                                    public void onResponse(String string) {
                                                                        handler.post(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                configRV(context);
                                                                                Toast.makeText(context, R.string.toast_delete_success, Toast.LENGTH_SHORT).show();
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

    //获取收藏
    private void getCollection(final String userName, final SimpleCallBack callBack) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String TAG = "get";
        final String CHECK_HOST = "http://chieching.cn/TranslateServer/Collection";
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
                params.put("userName", userName);
                params.put("requestType", RequestType.QUERY.toString());
                return params;
            }
        };
        request.setTag(TAG);
        queue.add(request);
    }

    //删除收藏
    private void deleteCollection(final String userName, final String content, final SimpleCallBack callBack) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String TAG = "delete";
        final String CHECK_HOST = "http://chieching.cn/TranslateServer/Collection";
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
                params.put("userName", userName);
                params.put("content", content);
                params.put("requestType", RequestType.DELETE.toString());
                return params;
            }
        };
        request.setTag(TAG);
        queue.add(request);
    }
}
