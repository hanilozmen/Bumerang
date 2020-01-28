package com.kokteyl.android.bumerang.response;

public interface ResponseListener<T> {
    void onSuccess(T response);
    void onError(T response);
}