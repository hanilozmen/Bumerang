package com.kokteyl.android.bumerang.response;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
public class ResponseListener<T> {

    private Type responseClassType;

    public ResponseListener() {
        ParameterizedType parameterizedType = ((ParameterizedType) getClass().getGenericSuperclass());
        if (parameterizedType != null) {
            Type[] types = parameterizedType.getActualTypeArguments();
            if (types.length > 0) {
                Type type = types[0];
                Type[] types1 = ((ParameterizedType) type).getActualTypeArguments();
                if (types1.length > 0) {
                    responseClassType = types1[0];
                }
            }
        }
    }

    Type getResponseClassType() {
        return responseClassType;
    }

    public void onSuccess(T response) {
    }

    public void onError(T response) {
    }
}