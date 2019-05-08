package com.tsitsing.translation.utility;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.tsitsing.translation.R;
import com.tsitsing.translation.emun.LearnedType;
import com.tsitsing.translation.emun.PlanAPIType;
import com.tsitsing.translation.interfaces.ActivityCallBack;
import com.tsitsing.translation.interfaces.SimpleCallBack;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReciteWordOptions {

    private int n;
    private String userName;
    private String planName;
    private Context context;
    private HashSet<String> initialSet = new HashSet<>();//存放已获取过的首字母
    private List<String> listTemp = new ArrayList<>();//临时存放未学单词
    private List<String> listInit = new ArrayList<>();//存放未学单词
    private List<String> listNow ;//list的copy，存放当前单词学习情况的变化，进行增删的list
    private int cursor;//当前单词的在listInit中的位置

    public ReciteWordOptions(String userName, String planName, Context context) {
        this.userName = userName;
        this.planName = planName;
        this.context = context;
    }

    //初始化list
    public void init(final ActivityCallBack call) {
        //获取recite表字段temp_word_set的情况
        getTWSContent(new SimpleCallBack() {
            @Override
            public void onResponse(String string) {
                try {
                    JSONObject jsonObject = new JSONObject(string);
                    n = Integer.parseInt(jsonObject.getString("numPerDay"));
                    if (!(jsonObject.has("tempWordSet")) || jsonObject.getString("tempWordSet").equals("[]")) {
                        //temp_word_set字段为空，调用下面的方法递归获取单词
                        getInitList(n, call);
                    } else {
                        //temp_word_set字段不为空，直接使用查询出来的单词
                        String wordSetStr = jsonObject.getString("tempWordSet");
                        wordSetStr = wordSetStr.replace("[", "");
                        wordSetStr = wordSetStr.replace("]", "");
                        wordSetStr = wordSetStr.replace(" ", "");
                        listInit = Arrays.asList(wordSetStr.split(","));
                        listNow = new ArrayList<>(listInit);
                        requestWord(listNow.get(0), call);
                        //初始化listInit的下标
                        cursor = 0;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //点击learned时调用
    public void learnedAction (final String word, final ActivityCallBack callBack) {
        if (listNow.contains(word)) {
            insertWordRequest(LearnedType.INSERT, word, new SimpleCallBack() {
                @Override
                public void onResponse(String string) {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("isLearned", "true");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    callBack.onResponse(jsonObject);
                }
            });
            listNow.remove(word);//修改当前list状态
            insertTWSContent(listNow.toString());//更新recite表的temp_word_set字段
        } else {
            //从已学中删除该单词
            insertWordRequest(LearnedType.DELETE, word, new SimpleCallBack() {
                @Override
                public void onResponse(String string) {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("isLearned", "false");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    callBack.onResponse(jsonObject);
                    listNow.add(word);
                    insertTWSContent(listNow.toString());//更新recite表的temp_word_set字段
                }
            });
        }
    }

    //上一页调用
    public void upPage (final ActivityCallBack callBack) {
        if (cursor == 0) {
//            Toast.makeText(context, R.string.first_word, Toast.LENGTH_SHORT).show();
        } else {
            cursor --;
            requestWord(listInit.get(cursor % listInit.size()), new ActivityCallBack() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    boolean isLearned = !listNow.contains(listInit.get(cursor % listInit.size()));
                    try {
                        jsonObject.put("isLearned", isLearned);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    callBack.onResponse(jsonObject);
                }
            });
        }
    }

    //下一页调用
    public void downPage (final ActivityCallBack callBack) {
        cursor ++;
        requestWord(listInit.get(cursor % listInit.size()), new ActivityCallBack() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                boolean isLearned = !listNow.contains(listInit.get(cursor % listInit.size()));
                try {
                    jsonObject.put("isLearned", isLearned);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                callBack.onResponse(jsonObject);
            }
        });
    }

    //递归获取单词
    private void getInitList (final int n, final ActivityCallBack call) {
        if (initialSet.size() != 52) {//判断是否还有可取出的单词。全部大小写字母
            requestDate(getInitial(), new SimpleCallBack() {
                @Override
                public void onResponse(String string) {
                    if (listInit.size() < n) {
                        for (int i = 0; (i < n - listInit.size()) && i < listTemp.size(); i++) {
                            listInit.add(listTemp.get(i));
                        }
                        if (listInit.size() < n) {
                            getInitList(n, call);
                        } else {
                            listNow = new ArrayList<>(listInit);
                            //将list转字符串插入到recite表的temp_word_set中
                            insertTWSContent(listNow.toString());
                            requestWord(listInit.get(0), call);
                            //初始化listInit的下标
                            cursor = 0;
                        }
                    }
                }
            });
        } else {
            listNow = listInit;
            //将list转字符串插入到recite表的temp_word_set中
            insertTWSContent(listNow.toString());
            if (!listNow.isEmpty()) {
                requestWord(listInit.get(0), call);
                //初始化listInit的下标
                cursor = 0;
            } else {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("isDone", "TRUE");//所有单词已经背完
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                call.onResponse(jsonObject);
            }
        }
    }

    //插入删除已学单词请求
    private void insertWordRequest (final LearnedType type, final String word, final SimpleCallBack callBack) {
        final String LEARNED_HOST = "http://chieching.cn/TranslateServer/InsertLearned";
        final String TAG = "learned";
        final RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.cancelAll(TAG);
        StringRequest request = new StringRequest(Request.Method.POST, LEARNED_HOST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.equals("TRUE")) {
                    callBack.onResponse("");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            protected Map<String, String> getParams() {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
                String addDate = dateFormat.format(new Date());
                Map<String, String> params = new HashMap<>();
                params.put("type", type.toString());
                params.put("userName", userName);
                params.put("planName", planName);
                params.put("word", word);
                params.put("addDate", addDate);
                return params;
            }
        };
        request.setTag(TAG);
        requestQueue.add(request);
    }

    //获取recite表的temp_word_set的内容
    private void getTWSContent(final SimpleCallBack callBack) {
        final String TWS_HOST = "http://chieching.cn/TranslateServer/Plan";
        final String TAG = "tws";
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.cancelAll(TAG);
        StringRequest getTWSRequest = new StringRequest(Request.Method.POST, TWS_HOST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("_______response from getTWSContent()_____", response);
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
                params.put("planName", planName);
                params.put("type", PlanAPIType.GET_INFO.toString());
                return params;
            }
        };
        getTWSRequest.setTag(TAG);
        requestQueue.add(getTWSRequest);
    }

    //将listNow插入数据库的方法
    private void insertTWSContent(final String tempWordSet) {
        final String TWS_HOST = "http://chieching.cn/TranslateServer/TempWordSet";
        final String TAG = "insertTempWordSet";
        final RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.cancelAll(TAG);
        StringRequest insertTWSRequest = new StringRequest(Request.Method.POST, TWS_HOST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            protected Map<String, String> getParams() {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
                String date = format.format(new Date());
                Map<String, String> params = new HashMap<>();
                params.put("userName", userName);
                params.put("planName", planName);
                params.put("date", date);
                params.put("list", tempWordSet);
                return params;
            }
        };
        insertTWSRequest.setTag(TAG);
        requestQueue.add(insertTWSRequest);
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

    //获取某首字母的未学过的单词集,合并到list中
    private void requestDate(final String initial, final SimpleCallBack callBack) {
        final String WORD_SET_HOST = "http://chieching.cn/TranslateServer/WordSet";
        final String TAG = "wordSet";
        final RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.cancelAll(TAG);
        StringRequest request = new StringRequest(Request.Method.POST, WORD_SET_HOST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("______test____", "");
                String reStr = response.replace("[", "");
                reStr = reStr.replace("]", "");
                reStr = reStr.replace(" ", "");
                listTemp = Arrays.asList(reStr.split(","));
                callBack.onResponse("");
                Log.d("______listTemp_____", listTemp.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("______response error______", error.getMessage());
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
    private void requestWord(final String word, final ActivityCallBack callBack) {
        final String WORD_HOST = "http://chieching.cn/TranslateServer/SingleWord";
        final String TAG = "word";
        final RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.cancelAll(TAG);
        StringRequest request = new StringRequest(Request.Method.POST, WORD_HOST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    callBack.onResponse(new JSONObject(response));
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
                params.put("word", word);
                return params;
            }
        };
        request.setTag(TAG);
        requestQueue.add(request);
    }
}
