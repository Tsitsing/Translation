package com.tsitsing.translation;

import android.app.Application;

public class MyApplication extends Application {
    private boolean isSignIn = false;//登录状态
    private String userName = null;//用户名

    public boolean getIsSignIn () {
        return this.isSignIn;
    }

    public void setIsSignIn (boolean state){
        this.isSignIn = state;
    }

    public String getUserName () {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
