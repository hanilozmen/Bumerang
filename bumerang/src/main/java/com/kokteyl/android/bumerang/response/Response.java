package com.kokteyl.android.bumerang.response;

import com.kokteyl.android.bumerang.core.Bumerang;
import com.kokteyl.android.bumerang.core.BumerangError;
import com.kokteyl.android.bumerang.core.BumerangLog;

import java.io.FileNotFoundException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Response<T> {

    private ResponseItem errorRawItem;
    private ResponseItem successRawItem;
    private Map<String, List<String>> responseHeaders;
    private Response<T> cachedResponse;
    private transient ResponseListener mListener;
    private boolean isFromCache;

    public static int MIN_SUCCESS_HTTP_CODE = 200;
    public static int MAX_SUCCESS_HTTP_CODE = 299;

    public boolean isSuccess() {
        return errorRawItem == null && successRawItem != null;
    }

    public boolean is200() {
        return isSuccess() && successRawItem.httpCode == 200;
    }

    public boolean isFromCache() {
        return isFromCache;
    }

    public void setFromCache(boolean fromCache) {
        isFromCache = fromCache;
    }

    public void setListener(ResponseListener listener) {
        mListener = listener;
    }

    private Response() {}

    public Response(String rawResponse, int responseCode, Map<String, List<String>> responseHeaders) {
        this.responseHeaders = responseHeaders;
        if (responseCode >= MIN_SUCCESS_HTTP_CODE && responseCode <= MAX_SUCCESS_HTTP_CODE) {
            successRawItem = new ResponseItem(rawResponse, responseCode);
            errorRawItem = null;
        } else {
            successRawItem = null;
            errorRawItem = new ResponseItem(rawResponse, responseCode);
        }
    }

    public Response(BumerangError error, Throwable exception) {
        successRawItem = null;
        errorRawItem = new ResponseItem(error.getMessage(), error.getCode(), exception);
    }

    public void setCachedResponse(Response<T> cachedResponse) {
        this.cachedResponse = cachedResponse;
    }

    public T getResponse() {
        if (successRawItem == null) return null;
        try {
            Type responseClassType = mListener.getResponseClassType();
            return Bumerang.get().gson().fromJson(successRawItem.getRawResponse(), responseClassType);
        } catch (Exception e) {
            BumerangLog.w("Error while converting response body!", e);
            return null;
        }
    }

    public Response<T> getCache() {
        return this.cachedResponse;
    }

    public ResponseItem getSuccessRawItem() {
        return successRawItem;
    }

    public ResponseItem getErrorRawItem() {
        return errorRawItem;
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "\n%s%s\n<-----", successRawItem != null ? successRawItem.toString() : "", errorRawItem != null ? errorRawItem.toString() : "");
    }

    public static class ResponseItem {

        String rawResponse;
        int httpCode;
        Throwable exception;

        public String getRawResponse() {
            return rawResponse;
        }

        @Override
        public String toString() {
            String exceptionStr = "";
            if (exception instanceof FileNotFoundException) {
                exceptionStr = "Server returned 404";
            } else if (exception != null) {
                exceptionStr = exception.toString();
            }
            return String.format(Locale.ENGLISH, "code: %d\nbody: %s %s ", httpCode, rawResponse != null ? rawResponse : "", exception != null ? ", exception: " + exceptionStr : "");
        }

        public ResponseItem(String rawResponse, int responseCode) {
            if (responseCode >= MIN_SUCCESS_HTTP_CODE && responseCode <= MAX_SUCCESS_HTTP_CODE) {
                this.rawResponse = rawResponse;
                this.httpCode = responseCode;
            } else {
                BumerangLog.i(String.format(Locale.ENGLISH, "Server error code: %d , raw response: %s", responseCode, rawResponse));
                this.rawResponse = rawResponse;
                this.httpCode = responseCode;
            }
        }

        public ResponseItem(String localMessage, int localCode, Throwable exception) {
            //BumerangLog.i(String.format(Locale.ENGLISH, "Error: %d , message: %s , exception: %s", localCode, rawResponse, exception.toString()));
            this.rawResponse = localMessage;
            this.httpCode = localCode;
            this.exception = exception;
        }
    }
}
