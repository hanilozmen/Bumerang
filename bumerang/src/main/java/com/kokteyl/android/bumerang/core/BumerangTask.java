package com.kokteyl.android.bumerang.core;

import android.os.AsyncTask;

import com.kokteyl.android.bumerang.request.Request;
import com.kokteyl.android.bumerang.response.HTTPCache;
import com.kokteyl.android.bumerang.response.Response;
import com.kokteyl.android.bumerang.response.ResponseListener;

import java.util.Locale;

public class BumerangTask<S extends Request<T>, T> extends AsyncTask<S, Integer, Response<T>> {

    private S request;
    private Response<T> response;
    private ResponseListener<Response<T>> listener;
    private HTTPCache<T> cache;

    public BumerangTask(ResponseListener<Response<T>> listener) {
        this.listener = listener;
    }

    @Override
    protected Response<T> doInBackground(S... requests) {
        request = requests[0];
        BumerangLog.d(request.toString());
        if (!request.dontCache()) cache = Bumerang.getFromCache(request.getCacheKey());
        if (cache != null && !cache.isExpired()) {
            response = cache.getResponse();
            response.setFromCache(true);
        } else {
            response = request.performRequest(request.getTypeName());
            response.setFromCache(false);
        }
        if (cache != null) response.setCachedResponse(cache.getResponse());
        if (response != null) {
            response.setListener(this.listener);
        }
        return response;
    }

    @Override
    protected void onPostExecute(Response<T> response) {
        if (response == null) return;
        if (response.isSuccess()) {
            BumerangLog.d(String.format(Locale.ENGLISH, "<----- %s %s Success Response: %s %s", request.getTypeName(), request.getHost(), getCacheRemainingTime(), response.toString()));
            listener.onSuccess(response);
        } else {
            response.setFromCache(true);
            BumerangLog.v(String.format(Locale.ENGLISH, "<----- %s %s Response: %s %s%s", request.getTypeName(), request.getHost(), getCacheRemainingTime(), response.toString(), cache == null ? "" : "\n" + cache.toString()));
            listener.onError(response);
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
    }

    private String getCacheRemainingTime() {
        int remainingExpirationMs = cache == null ? 0 : cache.getRemainingExpirationTime();
        if (remainingExpirationMs == 0) return "";
        int hours = remainingExpirationMs / 3600;
        int minutes = (remainingExpirationMs % 3600) / 60;
        int seconds = remainingExpirationMs % 60;
        return String.format("(From Cache, Expires In: %02dh %02dm %02ds)", hours, minutes, seconds);
    }
}
