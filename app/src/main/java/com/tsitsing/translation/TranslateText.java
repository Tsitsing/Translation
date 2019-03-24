package com.tsitsing.translation;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * 文本翻译封装类
 * 使用volley框架进行网络请求
 */
class TranslateText {
    private String result = null;

    /**
     * 翻译
     * @param basicCallBack 回调方法
     * @param context 上下文
     * @param query 需要翻译的文本
     * @param from 源语言
     * @param to 目标语言
     */
    void transRequest(final BasicCallBack basicCallBack, Context context, final String query, final String from, final String to){
        final String TRANS_API_HOST="http://api.fanyi.baidu.com/api/trans/vip/translate";
        final String APP_ID="20190211000265342";
        final String SECURITY_KEY="P0tTPdrqOvttQqAeRqhF";
        String tag = "trans";
        RequestQueue requestQueue= Volley.newRequestQueue(context);
        requestQueue.cancelAll(tag);

        final StringRequest stringRequest = new StringRequest(Request.Method.POST, TRANS_API_HOST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Log.d("------------",response);
                    JSONObject jsonObject = new JSONObject(response);
                    String transResult = jsonObject.getString("trans_result");
                    transResult = transResult.replace("[","");
                    JSONObject jsonObject1 = new JSONObject(transResult);
                    //获取翻译结果
                    result = jsonObject1.getString("dst");
                    Log.d("result after TranslateText",result);
                    //调用 成功 回调方法
                    basicCallBack.doSuccess(result);
                } catch (JSONException e) {
                    Log.e("TAG", e.getMessage(), e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("TAG",error.getMessage(),error);
            }
        }){
            protected Map<String,String> getParams() {
                Map<String,String> params = new HashMap<>();
                params.put("q",query);
                params.put("from",from);
                params.put("to",to);
                params.put("appid",APP_ID);
                String salt = String.valueOf(System.currentTimeMillis());
                params.put("salt",salt);
                String src = APP_ID+query+salt+SECURITY_KEY;
                try {
                    params.put("sign",MD5.md5(src));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return params;
            }
        };
        stringRequest.setTag(tag);
        requestQueue.add(stringRequest);
    }
}
