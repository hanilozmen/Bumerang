package com.kokteyl.bumerang.sample.network;

import com.kokteyl.android.bumerang.core.Bumerang;

public class ResponseModel {
    private int userId;
    private int id;
    private String title;
    private boolean completed;

    public ResponseModel(int userId, int id, String title, boolean completed) {
        this.userId = userId;
        this.id = id;
        this.title = title;
        this.completed = completed;
    }

    @Override
    public String toString() {
        return Bumerang.get().gson().toJson(this);
    }
}
