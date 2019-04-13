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
import com.tsitsing.translation.emun.LearnedType;
import com.tsitsing.translation.interfaces.ActivityCall;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

class SingleProcedure {
    private static SingleProcedure instance;//实例
    private JSONArray jsonArray;//存放当前的JSONArray
    private HashSet<String> initialSet = new HashSet<>();//存放已获取过的首字母
    private HashSet<String> indexSet = new HashSet<>();//存放已获取过的下标
    private String userName, planName;
    private ArrayList<String> history = new ArrayList<>();//存放已显示过的单词
    private int cursor;//当前单词的在history中的位置
    private HashSet<String> learnedSet = new HashSet<>();//暂存已学单词
    private boolean flag = true;

    //私有构造方法
    private SingleProcedure(String userName, String planName) {
        this.userName = userName;
        this.planName = planName;
    }

    //通过此方法获取唯一的实例
    static SingleProcedure getInstance(String userName, String planName) {
        if (instance == null && userName != null && planName != null) {
            instance = new SingleProcedure(userName, planName);
        }
        return instance;
    }

    //初始化界面时调取数据
    void init(Context context, final ActivityCall activityCall) {
        String initial = getInitial();
        if (!initial.equals("NONE")) {
            requestDate(initial, context, new BasicCallBack() {
                @Override
                public void doSuccess() {
                    JSONObject jsonObject;
                    if (jsonArray.length() != 0) {
                        try {
                            int index;
                            index = getIndex(jsonArray);//获取随机不重复下标
                            jsonObject = jsonArray.getJSONObject(index);
                            history.add(jsonObject.getString("word"));
                            cursor = history.size() - 1;
                            jsonObject.put("isLearned", false);
                            activityCall.onResponse(jsonObject);//回调，返回DetailActivity继续处理
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("JSONException", "do success error");
                        }
                    } else {
                        Log.d("ListArray error", "arrayCache is null");
                    }
                }

                @Override
                public void doSuccess(String string) {
                }

                @Override
                public void doFail() {
                    Log.d("request error", "error");
                }
            });
        } else {
            //TODO:单词已背完
        }
    }

    //点击Learned的时候调用
    void learned(String word, Context context, final ActivityCall call) {
        final boolean isLearned = learnedSet.contains(word);
        LearnedType type;
        if (isLearned) {
            learnedSet.remove(word);
            type = LearnedType.DELETE;
        } else {
            type = LearnedType.INSERT;
            learnedSet.add(word);
        }
        requestLearned(type, word, context, new BasicCallBack() {
            @Override
            public void doSuccess() {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("isLearned", !isLearned);
                    call.onResponse(jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void doSuccess(String string) {

            }

            @Override
            public void doFail() {

            }
        });

    }

    //获取下一页时调用
    void downPage(final ActivityCall call, final Context context) {
        if (cursor == (history.size() - 1)) {//此时history中无下一个，需要取出新的JSONObject
            if (jsonArray.length() != 0) {
                try {
                    int index = getIndex(jsonArray);//获取随机不重复下标
                    if ((cursor + 1) % 10 == 0) {
                        flag = !flag;//控制判断的标志，去掉会死循环
                    }
                    if (index != -1 && flag) {//同一首字母的最多只取10个单词
                        JSONObject jsonObject = jsonArray.getJSONObject(index);
                        history.add(jsonObject.getString("word"));
                        cursor++;
                        boolean isLearned = learnedSet.contains(jsonObject.getString("word"));
                        jsonObject.put("isLearned", isLearned);//返回一个判断标志
                        call.onResponse(jsonObject);
                    } else {
                        String initial = getInitial();
                        if (!initial.equals("NONE")) {//如果还能生成不重复的首字母
                            requestDate(initial, context, new BasicCallBack() {
                                @Override
                                public void doSuccess() {
                                    indexSet.clear();//清空上组存储的下标
                                    downPage(call, context);//继续递归调用此方法
                                }

                                @Override
                                public void doSuccess(String string) {

                                }

                                @Override
                                public void doFail() {

                                }
                            });
                        } else {
                            //TODO 单词已背完
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("JSONException", "do success error");
                }
            } else {
                Log.d("ListArray error", "arrayCache is null");
            }
        } else {//从history中取词查询
            cursor++;
            requestWord(history.get(cursor), context, new BasicCallBack() {
                @Override
                public void doSuccess() {

                }

                @Override
                public void doSuccess(String string) {
                    try {
                        JSONObject jsonObject = new JSONObject(string);
                        boolean isLearned = learnedSet.contains(jsonObject.getString("word"));
                        jsonObject.put("isLearned", isLearned);
                        call.onResponse(jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void doFail() {

                }
            });
        }
    }

    //获取上一页时调用
    void upPage(final ActivityCall call, Context context) {
        if (cursor != 0) {
            cursor--;//游标移至上一个元素
            requestWord(history.get(cursor), context, new BasicCallBack() {
                @Override
                public void doSuccess() {

                }

                @Override
                public void doSuccess(String string) {
                    try {
                        JSONObject jsonObject = new JSONObject(string);
                        boolean isLearned = learnedSet.contains(jsonObject.getString("word"));
                        jsonObject.put("isLearned", isLearned);//判断是否已学标记
                        call.onResponse(jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void doFail() {

                }
            });
        }
    }

    //随机不重复获取jsonArray下标
    private int getIndex(JSONArray jsonArray) {
        int index = -1;
        final int MAX = jsonArray.length();
        do {
            index = (int) (Math.random() * MAX);
        }
        while (!indexSet.add(Integer.toString(index)) && indexSet.size() < MAX);//当此下标已存在集合中且下标未全部用完时重复
        return index;
    }

    //随机生成不重复首字母
    private String getInitial() {
        String initial = "NONE";
        do {
            int num = (int) (Math.random() * 52);
            String initialRange = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
            initial = String.valueOf(initialRange.charAt(num));
        } while (!initialSet.add(initial));//该字母已存在于集合中时重复上一步
        if (initial.equals("NONE")) {//TODO：还缺少一个条件，当计划还没完成时才可重置set
            initialSet.clear();
        }
        return initial;
    }

    //获取未学过的单词集
    private void requestDate(final String initial, Context context, final BasicCallBack callBack) {
        final String WORD_SET_HOST = "http://chieching.cn/TranslateServer/WordSet";
        final String TAG = "wordSet";
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.cancelAll(TAG);
        StringRequest request = new StringRequest(Request.Method.POST, WORD_SET_HOST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    jsonArray = new JSONArray(response);
                    //根据请求类型判断需要回调的方法
                    callBack.doSuccess();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callBack.doFail();
                Log.d("request JSONArray, response error", error.getMessage());
            }
        }) {
            protected Map<String, String> getParams() {
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

    //查询单个单词
    private void requestWord(final String word, Context context, final BasicCallBack callBack) {
        final String WORD_HOST = "http://chieching.cn/TranslateServer/SingleWord";
        final String TAG = "word";
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.cancelAll(TAG);
        StringRequest request = new StringRequest(Request.Method.POST, WORD_HOST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callBack.doSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("word", word);
                return params;
            }
        };
        request.setTag(TAG);
        requestQueue.add(request);
    }

    //插入删除已学单词请求
    private void requestLearned(final LearnedType type, final String word, Context context, final BasicCallBack callBack) {
        final String LEARNED_HOST = "http://chieching.cn/TranslateServer/InsertLearned";
        final String TAG = "learned";
        final RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.cancelAll(TAG);
        StringRequest request = new StringRequest(Request.Method.POST, LEARNED_HOST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.equals("TRUE")) {
                    Log.d("+++++++++++", "success");
                    callBack.doSuccess();
                }
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
                params.put("word", word);
                return params;
            }
        };
        request.setTag(TAG);
        requestQueue.add(request);
    }
}
