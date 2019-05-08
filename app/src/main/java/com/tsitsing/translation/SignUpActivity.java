package com.tsitsing.translation;

import android.content.Context;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {
    EditText editMail;
    EditText editUserName;
    EditText editPassword;
    EditText editRepeatPassword;
    ImageView imgMail, imgUserName, imgPassword, imgRePassword;
    String mail;
    String userName;
    String password = "";//防止先点击重复密码时触发空指针异常
    Boolean isMail = false, isPassword = false, isRepeatPassword = false, isUserName = false;
    Handler handler = new Handler();
    RequestQueue requestQueue;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        editMail = findViewById(R.id.register_mail);
        editPassword = findViewById(R.id.register_password);
        editRepeatPassword = findViewById(R.id.register_repeat_password);
        editUserName = findViewById(R.id.register_userName);
        imgMail = findViewById(R.id.image_mail);
        imgUserName = findViewById(R.id.image_userName);
        imgPassword = findViewById(R.id.image_password);
        imgRePassword = findViewById(R.id.image_rePassword);
        context = getApplicationContext();
        requestQueue = Volley.newRequestQueue(context);
        //检查注册信息
        checkLegality();
        Button btnSubmit = findViewById(R.id.button_submit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("__________", "onclick");
                if (isMail && isUserName && isPassword && isRepeatPassword) {
                    infoRequest();
                } else {
                    Toast.makeText(context, R.string.submit, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    //检查内容是否合法
    private void checkLegality () {
        //检查邮箱
        editMail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                new Thread() {
                    @Override
                    public void run() {
                        mail = editMail.getText().toString();
                        CharSequence sequence = mail;
                        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
                        Matcher matcher = pattern.matcher(sequence);
                        if ((!mail.equals("")) && matcher.matches()) {
                            checkRequest("BY_MAIL", mail);
                        } else {
                            imgMail.setBackground(getDrawable(R.mipmap.wrong));
                        }
                    }
                }.start();
            }
        });
        //检查用户名
        editUserName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                new Thread() {
                    @Override
                    public void run() {
                        userName = editUserName.getText().toString();
                        if (!userName.equals(""))
                        checkRequest("BY_NAME", userName);
                    }
                }.start();
            }
        });
        //检查密码
        editPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                password = editPassword.getText().toString();
                if (password.length() >= 6) {
                    imgPassword.setBackground(getDrawable(R.mipmap.correct));
                    isPassword = true;
                } else {
                    imgPassword.setBackground(getDrawable(R.mipmap.wrong));
                    isRepeatPassword = false;
                }
            }
        });
        //检查两次密码是否符合
        editRepeatPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if ((password.length() >= 6) && ((editRepeatPassword.getText().toString()).equals(password))) {
                    imgRePassword.setBackground(getDrawable(R.mipmap.correct));
                    isRepeatPassword = true;
                } else {
                    imgRePassword.setBackground(getDrawable(R.mipmap.wrong));
                    isRepeatPassword = false;
                }
            }
        });
    }
    /**
     * 请求检查合法性
     * @param type "BY_NAME" or "BY_MAIL"
     */
    void checkRequest (final String type, final String token) {
        final String CHECK_HOST = "http://chieching.cn/TranslateServer/CheckLegality";
        final String TAG = "check";
        requestQueue.cancelAll(TAG);
        StringRequest request = new StringRequest(Request.Method.POST, CHECK_HOST, new Response.Listener<String>() {
            @Override
            public void onResponse(final String response) {
                handler.post(new Runnable() {//主线程需要做的事
                    @Override
                    public void run() {
                        if (response.equals("TRUE")) {
                            if (type.equals("BY_MAIL")) {
                                imgMail.setBackground(getDrawable(R.mipmap.correct));
                                isMail = true;
                            }
                            if (type.equals("BY_NAME")) {
                                imgUserName.setBackground(getDrawable(R.mipmap.correct));
                                isUserName = true;
                            }
                        } else {
                            if (type.equals("BY_MAIL")) {
                                imgMail.setBackground(getDrawable(R.mipmap.wrong));
                                isMail = false;
                            }
                            if (type.equals("BY_NAME")) {
                                imgUserName.setBackground(getDrawable(R.mipmap.wrong));
                                isUserName = false;
                            }
                        }
                    }
                });
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            protected Map<String, String> getParams (){
                Map<String, String> params = new HashMap<>();
                params.put("type", type);
                params.put("token", token);
                return params;
            }
        };
        request.setTag(TAG);
        requestQueue.add(request);
    }
    //发送注册信息
    void infoRequest ( ) {
        final String INFO_HOST = "http://chieching.cn/TranslateServer/RegisteAPI";
        final String TAG = "INFO";
        requestQueue.cancelAll(TAG);
        StringRequest request = new StringRequest(Request.Method.POST, INFO_HOST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("_______", response);
                if (response.equals("REGISTER_SUCCESS")) {
                    Toast.makeText(context, R.string.register_success, Toast.LENGTH_LONG).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    if (response.equals("SEND_MAIL_FAIL")) {
                        Toast.makeText(context, R.string.register_fail_sendMail, Toast.LENGTH_LONG).show();
                    }
                    if (response.equals("SAVE_FAIL")) {
                        Toast.makeText(context, R.string.register_fail, Toast.LENGTH_LONG).show();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            protected Map<String, String> getParams () {
                Map<String, String> params = new HashMap<>();
                params.put("address", mail);
                params.put("userName", userName);
                params.put("password", password);
                return params;
            }
        };
        request.setTag(TAG);
        requestQueue.add(request);
    }
}
