package com.kokteyl.android.bumerang.request;

import com.google.gson.JsonElement;
import com.kokteyl.android.bumerang.core.Bumerang;
import com.kokteyl.android.bumerang.core.BumerangError;
import com.kokteyl.android.bumerang.core.BumerangLog;
import com.kokteyl.android.bumerang.response.Response;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Set;

public class PostRequest<T> extends Request<T> {

    private boolean mFormUrlEncoded;

    PostRequest(String customCacheKey, String host, Map<String, String> headers, JsonElement params, boolean formUrlEncoded, int... timeoutValues) {
        setCacheKey(customCacheKey);
        setmFormUrlEncoded(formUrlEncoded);
        setHost(host);
        setParams(params);
        setHeaders(headers);
        setTimeout(timeoutValues);
    }

    private void setmFormUrlEncoded(boolean mFormUrlEncoded) {
        this.mFormUrlEncoded = mFormUrlEncoded;
        if (mFormUrlEncoded)
            addHeader(CONTENT_TYPE_KEY, URL_ENCODED_CONTENT_VALUE);
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
