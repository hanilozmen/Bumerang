package com.kokteyl.android.bumerang.response;

import com.kokteyl.android.bumerang.core.Bumerang;
import com.kokteyl.android.bumerang.core.BumerangPrefs;
import com.kokteyl.android.bumerang.request.Request;

import java.lang.reflect.Type;

public class HTTPCache<T> implements Cacheable {
    Request<T> request;
    Response<T> response;
    String cacheKey;
    long expireAt;

    public Response<T> getResponse() {
        return response;
    }

    public Request<T> getRequest() {
        return request;
    }

    public long getExpireAt() {
        return expireAt;
    }

    private HTTPCache() {
    }

    public HTTPCache(Request<T> request, Response<T> response, long expireAt, String cacheKey) {
        this.request = request;
        this.response = response;
        this.cacheKey = cacheKey;
        this.expireAt = expireAt;
    }

    public HTTPCache(Request<T> request, Response<T> response, long expireAt) {
        this.request = request;
        this.response = response;
        this.cacheKey = request.getCacheKey();
        this.expireAt = expireAt;
    }

    @Override
    public String toString() {
        if(response !=null)
            return response.toString();
        return "";
    }


    @Override
    public String putToCache() {
        return BumerangPrefs.instance().put(getCacheKey(), this);
    }

    @Override
    public HTTPCache getFromCache() {
        return BumerangPrefs.instance().get(getCacheKey(), HTTPCache.class);
    }

    @Override
    public String getCacheKey() {
        return cacheKey;
    }

    @Override
    public long getExpiresAt() {
        return expireAt;
    }

    @Override
    public boolean isExpired() {
        return getExpiresAt() - System.currentTimeMillis() <= 0;
    }

    @Override
    public int getRemainingExpirationTime() {
        return (int) ((getExpiresAt() - System.currentTimeMillis()) / 1000);
    }
}
