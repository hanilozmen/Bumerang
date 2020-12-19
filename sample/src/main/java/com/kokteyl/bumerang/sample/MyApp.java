package com.kokteyl.bumerang.sample;

import android.app.Application;

import com.google.gson.Gson;
import com.kokteyl.android.bumerang.core.Bumerang;


public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Bumerang bumerang = new Bumerang.Builder(getApplicationContext(),"https://jsonplaceholder.typicode.com/").gson(new Gson()).build();
    }
}
