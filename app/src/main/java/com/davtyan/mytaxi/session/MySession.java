package com.davtyan.mytaxi.session;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class MySession {

    private SharedPreferences prefs;

    public MySession(Context context) {
        // TODO Auto-generated constructor stub
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        //prefs = context.getSharedPreferences("myData",Context.MODE_PRIVATE);
    }

    public void setEmail(String email) {
        prefs.edit().putString("email", email).apply();
    }

    public String getEmail() {
        return prefs.getString("email","");
    }

    public void setPassword(String password) {
        prefs.edit().putString("password", password).apply();
    }

    public String getPassword() {
        return prefs.getString("password","");
    }

    public void setLogin(Boolean isLogin){
        prefs.edit().putBoolean("login", isLogin).apply();
    }

    public Boolean isLogin(){
        return prefs.getBoolean("login",false);
    }

}