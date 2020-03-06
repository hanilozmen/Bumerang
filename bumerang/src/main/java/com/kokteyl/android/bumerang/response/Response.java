package com.kokteyl.android.bumerang.response;

import com.kokteyl.android.bumerang.core.Bumerang;
import com.kokteyl.android.bumerang.core.BumerangError;
import com.kokteyl.android.bumerang.core.BumerangLog;
import com.kokteyl.android.bumerang.core.ResponseListener;

import java.io.FileNotFoundException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Response<T> {

    private ResponseItem errorRawItem;
    private ResponseItem successRawItem;
    private Map<String, List<String>> responseHeaders;
    private T cachedResponse;
    private Type mResponseType;
    private transient ResponseListener mListener;
    private boolean forwardToFailCallback;

    public static int MIN_SUCCESS_HTTP_CODE = 200;
    public static int MAX_SUCCESS_HTTP_CODE = 299;

    public boolean isSuccess() {
        return errorRawItem == null && successRawItem != null;
    }

    public Type getResponseType() {
        return mResponseType;
    }

    public void setResponseType(Type type) {
        mResponseType = type;
    }

    public boolean is200() {
        return isSuccess() && successRawItem.httpCode == 200;
    }

    public boolean isForwardToFailCallback() {
        return forwardToFailCallback;
    }

    public void forwardToFailCallback(boolean forward) {
        forwardToFailCallback = forward;
    }

    public void setListener(ResponseListener listener) {
        if (mListener == null) return;
        mListener = listener;
    }


    private Response() {
    }

    public Map<String, List<String>> getResponseHeaders() {
        return responseHeaders;
    }

    public Response(String rawResponse, int responseCode, Map<String, List<String>> responseHeaders, Exception exception) {
        this.responseHeaders = responseHeaders;
        if(exception !=null){
            exception.printStackTrace();
        }
        if (isBetweenMinAndMaxSuccessCodeRange(responseCode) && exception == null) {
            successRawItem = new ResponseItem(rawResponse, responseCode);
            errorRawItem = null;
        } else {
            successRawItem = null;
            errorRawItem = new ResponseItem(rawResponse, responseCode);
        }
    }

    public static boolean isBetweenMinAndMaxSuccessCodeRange(int responseCode) {
        return responseCode >= MIN_SUCCESS_HTTP_CODE && responseCode <= MAX_SUCCESS_HTTP_CODE;
    }

    public Response(BumerangError error, String rawErrorBody, Throwable exception) {
        successRawItem = null;
        errorRawItem = new ResponseItem(error.getMessage(), error.getCode(), exception);
    }

    public void setCachedResponse(Type type) {
        mResponseType = type;
        this.cachedResponse = getResponseInternal(type);
    }

    public T getResponse() {
        return getResponseInternal(getResponseType());
    }

    private T getResponseInternal(Type type) {
        if (successRawItem == null) return null;
        try {
            if (type == null)
                throw new RuntimeException("Response type is null");
            return Bumerang.get().gson().fromJson(successRawItem.getRawResponse(), getResponseType());
        } catch (Exception e) {
            BumerangLog.w("Error while converting response body!", e);
            return null;
        }
    }

    public T getCachedResponse() {
        return cachedResponse;
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
                exceptionStr = "FileNotFoundException";
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
