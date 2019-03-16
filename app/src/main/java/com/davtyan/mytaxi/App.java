package com.davtyan.mytaxi;

import android.app.Activity;
import android.app.Application;
import android.os.SystemClock;
import android.util.Log;

import com.davtyan.mytaxi.api.Constants;
import com.davtyan.mytaxi.api.Service;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class App extends Application {

    private static Service api;
    private Retrofit retrofit;

    @Override
    public void onCreate() {
        super.onCreate();
        SystemClock.sleep(700);

        retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(Service.class);
    }

    public static Service getApi() {
        return api;
    }

    public static void logClassName(Activity activity){
        Log.i("logClassName", "Name: " + activity.getLocalClassName());
    }
}

