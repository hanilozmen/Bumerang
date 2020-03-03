package com.kokteyl.android.bumerang.request;

import com.google.gson.JsonElement;
import com.kokteyl.android.bumerang.core.Bumerang;
import com.kokteyl.android.bumerang.core.BumerangLog;

import java.util.Map;

public class PostRequest<T> extends Request<T> {

    private boolean mFormUrlEncoded;

    PostRequest(String customCacheKey, String host, Map<String, String> headers, JsonElement params, boolean dontCache, boolean formUrlEncoded, int... timeoutValues) {
        setmFormUrlEncoded(formUrlEncoded);
        setHost(host);
        setParams(params);
        setHeaders(headers);
        setTimeout(timeoutValues);
        setDontCache(dontCache);
        setCacheKey(customCacheKey);
    }

    private void setmFormUrlEncoded(boolean mFormUrlEncoded) {
        this.mFormUrlEncoded = mFormUrlEncoded;
        if (mFormUrlEncoded)
            addHeader(CONTENT_TYPE_KEY, URL_ENCODED_CONTENT_VALUE + CHARSET_SUFFIX);
    }

    @Override
    public String getTypeName() {
        return "POST";
    }

    @Override
    public String getBody() {
        try {
            JsonElement params = getParams();
            String encoding = getEncoding();
            if (params == null) return null;
            if (mFormUrlEncoded) {
                try {
                    return urlEncodeParams(params, encoding);
                } catch (Exception e) {
                    BumerangLog.w("Post (Form BaseUrl Encoded)  : getBodyError", e);
                    return null;
                }
            } else {
                String jsonStr = Bumerang.get().gson().toJson(params);
                return jsonStr;
            }
        } catch (Exception e) {
            BumerangLog.w("Post (Raw  body) : getBodyError", e);
        }
        return null;
    }

}
