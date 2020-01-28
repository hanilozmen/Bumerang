package com.kokteyl.bumerang.sample;

import android.app.Application;

import com.kokteyl.android.bumerang.core.Bumerang;


public class MyApp extends Application {
    Bumerang bumerang;

    @Override
    public void onCreate() {
        super.onCreate();
        bumerang = new Bumerang.Builder(getApplicationContext(),"https://jsonplaceholder.typicode.com/").build();
    }
}
