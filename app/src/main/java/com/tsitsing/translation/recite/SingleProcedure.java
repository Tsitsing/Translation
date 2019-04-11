package com.tsitsing.translation.recite;

import android.content.Context;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.tsitsing.translation.BasicCallBack;
import com.tsitsing.translation.interfaces.ActivityCall;
import com.tsitsing.translation.interfaces.InitCallBack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

class SingleProcedure {
    private static SingleProcedure instance;
    private JSONArray[] arrayCache= new JSONArray[5];
    private HashSet<String> initialSet = new HashSet<>();
    private HashSet<String> arrayIndex = new HashSet<>();
    private int i = 0;
    private InitCallBack callBack;

    private SingleProcedure () {
    }

    static SingleProcedure getInstance() {
        if (instance == null) {
            instance = new SingleProcedure();
        }
        return instance;
    }
    //初始化界面时调取数据
    void init (String userName, String planName, Context context, final ActivityCall activityCall){
        Log.d("__________", "do init");
        String initial;//单词首字母
        do {
            int num = (int) (Math.random() * 52);
            String initialRange = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
            initial = String.valueOf(initialRange.charAt(num));
            Log.d("____initial char______", initial);
        } while (!initialSet.add(initial));
        requestDate(userName, planName, initial, context);
        callBack = new InitCallBack() {
            @Override
            public void onSuccess() {
                JSONObject jsonObject;
                int index;
                if (arrayCache.length != 0) {
                    try {
                        JSONArray jsonArray = arrayCache[0];
                        index = getIndex(jsonArray);
                        Log.d("_______index________", Integer.toString(index));
                        jsonObject = jsonArray.getJSONObject(index);
                        Log.d("________jsonObject________",jsonObject.toString() );
                        activityCall.onResponse(jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d("_________________", "do success error");
                    }
                } else {
                    Log.d("_________________", "arrayCache[]为空");
                }
            }
        };
    }
    //随机不重复获取jsonArray下标
    private int getIndex (JSONArray jsonArray) {
        int index;
        final int MAX = jsonArray.length();
        do {
            index = (int) (Math.random() * MAX);
        } while (!arrayIndex.add(Integer.toString(index)));//TODO:当该数组已满时需要处理
        return index;
    }
    //获取未学过的单词集
    private void requestDate (final String userName, final String planName, final String initial, Context context) {
        final String WORD_SET_HOST = "http://chieching.cn/TranslateServer/WordSet";
        final String TAG = "wordSet";
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.cancelAll(TAG);
        StringRequest request = new StringRequest(Request.Method.POST, WORD_SET_HOST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray wordSet = new JSONArray(response);
                    Log.d("______wordSet___________", wordSet.toString());
                    i++;
                    arrayCache[i-1] = wordSet;
                    callBack.onSuccess();
                }catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("json array request error", error.getMessage());
            }
        }) {
            protected Map<String, String> getParams () {
                Map<String, String> params = new HashMap<>();
                params.put("userName", userName);
                params.put("planName", planName);
                params.put("initial", initial);
                return params;
            }
        };
        request.setTag(TAG);
        requestQueue.add(request);
    }
}
