package com.kokteyl.android.bumerang.request;

import com.google.gson.JsonElement;
import com.kokteyl.android.bumerang.response.Response;

import java.util.Map;

public class DeleteRequest<T> extends GetRequest<T> {


    DeleteRequest(String customCacheKey, String host, Map<String, String> headers, JsonElement params, boolean dontCache, int... timeoutValues) {
        super(customCacheKey, host, headers, params, dontCache, timeoutValues);
    }

    @Override
    public String getTypeName() {
        return "DELETE";
    }


}
