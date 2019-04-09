package com.tsitsing.translation;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignInActivity extends AppCompatActivity {
    private static final int REQUEST_SIGN_UP = 2000;//注册请求码
    private EditText editUserName;
    private EditText editPassword;
    private String userName;
    private String password;
    private String type;
    Context context;
    RequestQueue requestQueue;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        context = getApplicationContext();
        requestQueue = Volley.newRequestQueue(context);
        editUserName = findViewById(R.id.sign_in_userName);
        editPassword = findViewById(R.id.sign_in_password);
        //前往注册页面
        TextView textRegister = findViewById(R.id.textView_register);
        textRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivityForResult(intent, REQUEST_SIGN_UP);
            }
        });
        //登录
        Button btnSignIn = findViewById(R.id.btn_sign_in);
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userName = editUserName.getText().toString();
                password = editPassword.getText().toString();
                //判断是否为邮箱格式
                String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
                CharSequence sequence = userName;
                Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(sequence);
                if (matcher.matches()) {
                    type = "BY_MAIL";
                } else {
                    type = "BY_NAME";
                }
                Log.d("_________", type);
                new Thread() {
                    @Override
                    public void run() {
                        signInRequest();
                    }
                }.start();
            }
        });
    }

    //发送登录请求
    void signInRequest() {
        final String TAG = "signIn";
        requestQueue.cancelAll(TAG);
        final String SIGN_IN_HOST = "http://chieching.cn/TranslateServer/SignIn";
        StringRequest request = new StringRequest(Request.Method.POST, SIGN_IN_HOST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    final JSONObject jsonObject = new JSONObject(response);
                    //如果用户存在
                    if (jsonObject.has("name")) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                MyApplication application = (MyApplication) getApplication();
                                try {
                                    //密码匹配
                                    if (jsonObject.getString("password").equals(password)) {
                                        if (jsonObject.getString("state").equals("1")) {
                                            application.setIsSignIn(true);
                                            application.setUserName(jsonObject.getString("name"));
                                            setResult(RESULT_OK);
                                            finish();
                                        } else {
                                            Toast.makeText(context, R.string.signIn_fail_mail, Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        Toast.makeText(context, R.string.signIn_fail_password, Toast.LENGTH_LONG).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(context, R.string.signIn_fail_userName, Toast.LENGTH_LONG).show();
                    }
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
                params.put("token", userName);
                params.put("type", type);
                return params;
            }
        };
        request.setTag(TAG);
        requestQueue.add(request);
    }
}
