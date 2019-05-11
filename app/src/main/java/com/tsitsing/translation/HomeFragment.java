package com.tsitsing.translation;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.tsitsing.translation.emun.CollectionType;
import com.tsitsing.translation.emun.RequestType;
import com.tsitsing.translation.interfaces.BasicCallBack;
import com.tsitsing.translation.interfaces.SimpleCallBack;
import com.tsitsing.translation.utility.MyDateUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private Spinner spinnerSource, spinnerDest;
    private EditText textInput;
    private TextView textResult;
    private String from;
    private String to;
    private String queryContent;
    private String result;
    private BasicCallBack tranCall;
    private RequestQueue requestQueue;
    private LinearLayout linearWords;
    private Handler handler = new Handler();
    private ImageView imgCollection;
    private MyApplication myApplication;

    private Button button;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        spinnerSource = view.findViewById(R.id.spinner_source);
        spinnerDest = view.findViewById(R.id.spinner_dest);
        textInput = view.findViewById(R.id.text_input);
        textResult = view.findViewById(R.id.text_result);
        linearWords = view.findViewById(R.id.linear_words);
        ScrollView scrollView = view.findViewById(R.id.scroll_home_words);
        ImageView ivReset = view.findViewById(R.id.iv_home_reset);
        imgCollection = view.findViewById(R.id.iv_home_collect);
        myApplication = (MyApplication)getActivity().getApplication();

        //收藏
        collectEvent(new SimpleCallBack() {
            @Override
            public void onResponse(final String string) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (string.equals("true")) {
                            imgCollection.setImageDrawable(getResources().getDrawable(R.mipmap.collection_fill, null));
                            Toast.makeText(getContext(), R.string.collection_success, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), R.string.collection_failure, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        //清空输入框内容
        ivReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textInput.setText("", TextView.BufferType.NORMAL);
            }
        });

        //通过回车键发送请求
        textInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    queryContent = textInput.getText().toString();
                    transRequest(queryContent);
                }
                return true;//是否消耗此事件
            }
        });

        //新建spinner的适配器，设配器中存放数据
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, getData());
        final ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<>(getContext(), R.layout.spinner_item, getData2());
        //设置适配器
        spinnerSource.setAdapter(arrayAdapter);
        spinnerDest.setAdapter(arrayAdapter2);
        //在指定位置弹出
        spinnerSource.setDropDownHorizontalOffset(50);
        spinnerSource.setDropDownVerticalOffset(110);
        spinnerDest.setDropDownHorizontalOffset(1030);
        spinnerDest.setDropDownVerticalOffset(110);

        //设置源语言选择监听事件
        spinnerSource.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SelectLanguage selectFrom = new SelectLanguage();
                from = selectFrom.getLangCode(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //设置目标语言选择监听事件
        spinnerDest.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SelectLanguage selectTo = new SelectLanguage();
                to = selectTo.getLangCode(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //交换源语言与目标语言
        ImageView imgSwitch = view.findViewById(R.id.img_home_switch);
        imgSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //先获取当前选中的item
                String srcSelected = (String) spinnerSource.getSelectedItem();
                String dstSelected = (String) spinnerDest.getSelectedItem();
                //通过适配器获取对应item的位置
                int srcTarget = arrayAdapter.getPosition(dstSelected);
                int dstTarget = arrayAdapter2.getPosition(srcSelected);
                if (srcTarget != -1 && dstTarget != -1) {//值为-1时说明不存在此item
                    //根据位置重新设定当前spinner的选择
                    spinnerSource.setSelection(srcTarget, true);
                    spinnerDest.setSelection(dstTarget, true);
                } else {
                    Toast.makeText(getContext(), R.string.toast_canNotSwitch, Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }

    //收藏按钮事件
    private void collectEvent (final SimpleCallBack callBack) {
        imgCollection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myApplication.getIsSignIn()) {
                    new Thread() {
                        @Override
                        public void run() {
                            final String URL = "http://chieching.cn/TranslateServer/Collection";
                            final String TAG = "collection";
                            final RequestQueue requestQueue = Volley.newRequestQueue(getContext());
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
                            }){
                                protected Map<String, String> getParams() {
                                    Map<String, String> params = new HashMap<>();
                                    params.put("userName", myApplication.getUserName());
                                    params.put("contentSource", textInput.getText().toString());
                                    params.put("contentAfterTrans", textResult.getText().toString());
                                    params.put("date", MyDateUtil.getSketchyDate());
                                    params.put("type", CollectionType.TEXT_TRANS.toString());//所收藏数据的类型
                                    params.put("requestType", RequestType.INSERT.toString());//请求类型为插入数据
                                    return params;
                                }
                            };
                            request.setTag(TAG);
                            requestQueue.add(request);
                        }
                    }.start();
                } else {//未登录，提示登录
                    Toast.makeText(getContext(), R.string.please_signIn, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private List<String> getData() {
        List<String> list = new ArrayList<>();
        list.add("自动");
        list.add("中文");
        list.add("英文");
        list.add("日语");
        list.add("法语");
        list.add("韩语");
        list.add("繁体");
        return list;
    }

    private List<String> getData2() {
        List<String> list = new ArrayList<String>();
        list.add("英文");
        list.add("中文");
        list.add("日语");
        list.add("法语");
        list.add("韩语");
        list.add("繁体");
        return list;
    }

    public void transRequest(final String query) {
        final String TRANS_API_HOST = "http://api.fanyi.baidu.com/api/trans/vip/translate";
        final String APP_ID = "20190211000265342";
        final String SECURITY_KEY = "P0tTPdrqOvttQqAeRqhF";
        final String tag = "trans";
        final String CORPUS_API_HOST = "http://chieching.cn/TranslateServer/Handler";
        final String tag_corpus = "corpus";
        requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.cancelAll(tag);
        //翻译请求
        final StringRequest stringRequest = new StringRequest(Request.Method.POST, TRANS_API_HOST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    StringBuilder stringBuilder = new StringBuilder();
                    JSONObject jsonObject = new JSONObject(response);
                    String transResult = jsonObject.getString("trans_result");
                    JSONArray jsonArray = new JSONArray(transResult);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        stringBuilder.append(jsonArray.getJSONObject(i).getString("dst"));
                    }
                    result = stringBuilder.toString();
                    textResult.setText(result);//输出结果
                    imgCollection.setImageDrawable(getResources().getDrawable(R.mipmap.collection, null));
//                    textResult.setVisibility(View.VISIBLE);//设置初始为不可见的textView为可见
                } catch (JSONException e) {
                    Log.e("TAG", e.getMessage(), e);
                }
                //回调，继续下一层请求
                tranCall = new BasicCallBack() {
                    @Override
                    public void doSuccess() {
                        if (to.equals("en")) {
                            //查询语料库，获取重点词汇信息
                            final StringRequest corpusRequest = new StringRequest(Request.Method.POST, CORPUS_API_HOST, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    linearWords.removeAllViews();//先清空之前添加的组件
                                    try {
                                        JSONArray jsonArray = new JSONArray(response);
                                        if (jsonArray.length() != 0) {
                                            JSONObject jsonObject;
                                            GenerateCard generateCard = new GenerateCard(getContext());
                                            for (int i = 0; i < jsonArray.length(); i++) {
                                                if (!jsonArray.isNull(i)) {
                                                    jsonObject = jsonArray.getJSONObject(i);
                                                    if (jsonObject.has("word")) {
                                                        String pronunciation = jsonObject.getString("pronunciation");
                                                        if (!pronunciation.equals("")) {
                                                            pronunciation = "[" + pronunciation + "]";
                                                        }
                                                        LinearLayout subCard = generateCard.getCar(
                                                                jsonObject.getString("word") + " "
                                                                        + pronunciation, jsonObject.getString("translation"));
                                                        linearWords.addView(subCard);//添加新生成的的组件
//                                                        scrollView.setVisibility(View.VISIBLE);
                                                    } else {
//                                                        scrollView.setVisibility(View.INVISIBLE);
                                                    }
                                                }
                                            }
                                        } else {
//                                            scrollView.setVisibility(View.INVISIBLE);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.d("++++++++++", "response error");
                                }
                            }) {
                                protected Map<String, String> getParams() {
                                    Map<String, String> params = new HashMap<>();
                                    params.put("params", result);
                                    Log.d("------params result------", result);
                                    return params;
                                }
                            };
                            corpusRequest.setTag(tag_corpus);
                            requestQueue.add(corpusRequest);
                        } else {
                            linearWords.removeAllViews();//清空之前添加的组件
//                            scrollView.setVisibility(View.INVISIBLE);
                        }
                    }

                    @Override
                    public void doSuccess(String string) {
                    }

                    @Override
                    public void doFail() {
                    }
                };
                tranCall.doSuccess();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("TAG", error.getMessage(), error);
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("q", query);
                params.put("from", from);
                params.put("to", to);
                params.put("appid", APP_ID);
                String salt = String.valueOf(System.currentTimeMillis());
                params.put("salt", salt);
                String src = APP_ID + query + salt + SECURITY_KEY;
                try {
                    params.put("sign", MD5.md5(src));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return params;
            }
        };
        stringRequest.setTag(tag);
        requestQueue.add(stringRequest);
    }


    //test
    private void forTest() {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity().getApplicationContext(), TestActivity.class);
                startActivity(intent);
            }
        });
    }
}
