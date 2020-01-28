package com.kokteyl.android.bumerang.request;

import com.google.gson.JsonElement;
import com.kokteyl.android.bumerang.response.Response;

import java.util.Map;

public class PutRequest<T> extends GetRequest<T> {


    PutRequest(String customCacheKey, String host, Map<String, String> headers, JsonElement params, int... timeoutValues) {
        super(customCacheKey,host, headers, params, timeoutValues);
    }

    @Override
    public String getTypeName() {
        return "PUT";
    }


}
