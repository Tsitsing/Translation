package com.tsitsing.translation.utility;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.tsitsing.translation.interfaces.ActivityCallBack;
import com.tsitsing.translation.interfaces.SimpleCallBack;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MySpaceOptions {
    private Context context;
    private String userName;

    public MySpaceOptions (Context context, String userName) {
        this.context = context;
        this.userName = userName;
    }

    //获取用户信息
    public void getUserInfo (final ActivityCallBack callBack) {
        final String URL = "http://chieching.cn/TranslateServer/UserInfo";
        final String TAG = "getUserInfo";
        final RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.cancelAll(TAG);
        StringRequest request = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    String str = response.replace("[", "");
                    str = str.replace("]", "");
                    callBack.onResponse(new JSONObject(str));
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
                return params;
            }
        };
        request.setTag(TAG);
        requestQueue.add(request);
    }

    //获取指定用户名的动态信息
    public void getDynamic (final String specifyUserName, final SimpleCallBack callBack) {
        final String URL = "http://chieching.cn/TranslateServer/Dynamic";
        final String TAG = "getDynamic";
        final RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.cancelAll(TAG);
        StringRequest request = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
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
                params.put("userName", specifyUserName);
                return params;
            }
        };
        request.setTag(TAG);
        requestQueue.add(request);
    }
}
