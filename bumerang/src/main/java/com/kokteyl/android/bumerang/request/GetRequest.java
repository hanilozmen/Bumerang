package com.kokteyl.android.bumerang.request;

import com.google.gson.JsonElement;
import com.kokteyl.android.bumerang.core.BumerangLog;

import java.util.Map;

public class GetRequest<T> extends Request<T> {

    GetRequest(String customCacheKey, String host, Map<String, String> headers, JsonElement params, boolean dontCache, int... timeoutValues) {
        setCacheKey(customCacheKey);
        setParams(params);
        setHeaders(headers);
        String encodedParams = isParamsEmpty() ? "" : new String(getBody());
        setHost(host + encodedParams);
        setTimeout(timeoutValues);
        setDontCache(dontCache);
    }


    @Override
    public String getBody() {
        try {
            return urlEncodeParams(getParams(), getEncoding());
        } catch (Exception e) {
            BumerangLog.w("getBodyError", e);
            return null;
        }
    }

    @Override
    public String getTypeName() {
        return "GET";
    }

}
