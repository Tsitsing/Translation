package com.tsitsing.translation;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class RegisterActivity extends AppCompatActivity {
    EditText editMail;
    EditText editUserName;
    EditText editPassword;
    EditText editRepeatPassword;
    String mail;
    String userName;
    String password;
    Boolean isMail, isPassword, isRepeatPassword, isUserName;
    Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        editMail = findViewById(R.id.register_mail);
        editPassword = findViewById(R.id.register_password);
        editRepeatPassword = findViewById(R.id.register_repeat_password);
        editUserName = findViewById(R.id.register_userName);
    }
    //检查内容是否合法
    private void checkLegality () {
        //检查邮箱
        editMail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                new Thread() {
                    @Override
                    public void run() {
                        mail = editMail.getText().toString();
                        //TODO:send request
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                //TODO:change the image
                            }
                        });
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
                        //TODO:send request
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                //TODO:change the image
                            }
                        });
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
                    //TODO:chang the image
                    isPassword = true;
                } else {
                    isRepeatPassword = false;
                }
            }
        });
        //检查两次密码是否符合
        editRepeatPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if ((editRepeatPassword.getText().toString()).equals(password)) {
                    //TODO:change the image
                    isRepeatPassword = true;
                } else {
                    isRepeatPassword = false;
                }
            }
        });
    }
}
