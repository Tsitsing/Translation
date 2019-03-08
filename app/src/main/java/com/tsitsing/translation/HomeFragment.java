package com.tsitsing.translation;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
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
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

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


    public HomeFragment() {
        // Required empty public constructor
    }

    private Spinner spinnerSource,spinnerDest;
    private Button btnTranslate;
    private EditText textInput;
    private TextView textResult;

    private String from;
    private String to;
    private String queryContent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);


        spinnerSource = view.findViewById(R.id.spinner_source);
        spinnerDest = view.findViewById(R.id.spinner_dest);
        textInput = view.findViewById(R.id.text_input);
        textResult = view.findViewById(R.id.text_result);

        textInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || (event !=null&&event.getKeyCode() == KeyEvent.KEYCODE_ENTER)){
                    queryContent = textInput.getText().toString();
                    transRequest(queryContent, from, to);
                }
                return true;
            }
        });

        //用于存放简单数据
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item,getData());
        spinnerSource.setAdapter(arrayAdapter);
        spinnerDest.setAdapter(arrayAdapter);
        spinnerSource.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (parent.getItemAtPosition(position).toString()){
                    case "中文":
                        from = "zh";
                        break;
                    case "英文":
                        from = "en";
                        break;
                    case "日语":
                        from = "jp";
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerDest.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (parent.getItemAtPosition(position).toString()){
                    case "中文":
                        to = "zh";
                        break;
                    case "英文":
                        to = "en";
                        break;
                    case "日语":
                        to = "jp";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        return view;
    }

    private List<String> getData(){
        List<String> list = new ArrayList<String>();
        list.add("中文");
        list.add("英文");
        list.add("日语");
        return list;
    }

    public void transRequest(final String query, final String from, final String to){
        final String TRANS_API_HOST="http://api.fanyi.baidu.com/api/trans/vip/translate";
        final String APP_ID="20190211000265342";
        final String SECURITY_KEY="P0tTPdrqOvttQqAeRqhF";
        String tag = "trans";
        RequestQueue requestQueue= Volley.newRequestQueue(getContext());
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
                    String result = jsonObject1.getString("dst");
                    textResult.setText(result);//输出结果
                    Log.d("-----------result",result);
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
