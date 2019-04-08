package com.tsitsing.translation;

import android.app.Application;

public class MyApplication extends Application {
    private boolean isSignIn = false;
    private String userName = null;

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
