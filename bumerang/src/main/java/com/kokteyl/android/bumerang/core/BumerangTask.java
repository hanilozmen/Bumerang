package com.kokteyl.android.bumerang.core;

import android.os.AsyncTask;

import com.kokteyl.android.bumerang.request.Request;
import com.kokteyl.android.bumerang.response.HTTPCache;
import com.kokteyl.android.bumerang.response.Response;

import java.lang.reflect.Type;
import java.util.Locale;

public class BumerangTask<S extends Request<T>, T> extends AsyncTask<S, Integer, Response<T>> {

    private S request;
    private Response<T> response;
    private ResponseListener<Response<T>> listener;
    private HTTPCache<T> httpCache;
    private Type mResponseType;

    public BumerangTask(ResponseListener<Response<T>> listener) {
        this.listener = listener;
        mResponseType = listener.getResponseClassType();
    }

    @Override
    protected Response<T> doInBackground(S... requests) {
        request = requests[0];
        BumerangLog.d(request.toString());
        if (!request.dontCache()) {
            httpCache = Bumerang.getFromCache(request.getCacheKey());
        }
        boolean isCacheAvailable = httpCache != null && httpCache.getResponse() != null;
        if (isCacheAvailable && !httpCache.isExpired()) {
            response = httpCache.getResponse();
            response.setCachedResponse(mResponseType);
            response.forwardToFailCallback(false);
        } else {
            Response remoteResponse = request.performRequest(request.getTypeName());
            if (remoteResponse.isSuccess()) {
                response = remoteResponse;
                response.forwardToFailCallback(false);
                response.setCachedResponse(mResponseType);
            } else if (isCacheAvailable) {
                response = httpCache.getResponse();
                response.forwardToFailCallback(true);
                response.setCachedResponse(mResponseType);
            } else {
                response = remoteResponse;
                response.forwardToFailCallback(true);
            }
        }
        response.setListener(listener);
        return response;
    }

    @Override
    protected void onPostExecute(Response<T> response) {
        if (!response.isForwardToFailCallback()) {
            BumerangLog.d(String.format(Locale.ENGLISH, "<----- %s %s Success Response %s %s", request.getTypeName(), request.getHost(), getCacheRemainingTime(), response.toString()));
            listener.onSuccess(response);
        } else {

            BumerangLog.v(String.format(Locale.ENGLISH, "<----- %s %s %s  %s %s", request.getTypeName(), request.getHost(), response.getCachedResponse() != null ? "Error: Cache Available" : "Error", getCacheRemainingTime(), response.toString()));
            listener.onError(response);
        }


    }

    @Override
    protected void onProgressUpdate(Integer... values) {
    }

    private String getCacheRemainingTime() {
        int remainingExpirationMs = httpCache == null ? 0 : httpCache.getRemainingExpirationTime();
        if (remainingExpirationMs <= 0) return "";
        int hours = remainingExpirationMs / 3600;
        int minutes = (remainingExpirationMs % 3600) / 60;
        int seconds = remainingExpirationMs % 60;
        return String.format("(From Cache, Expires In: %02dh %02dm %02ds)", hours, minutes, seconds);
    }
}
