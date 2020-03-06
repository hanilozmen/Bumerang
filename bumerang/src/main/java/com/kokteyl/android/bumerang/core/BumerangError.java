package com.kokteyl.android.bumerang.core;

import java.util.Locale;

public enum BumerangError {
    BASE_URL_ERROR(2000, "Request cancelled! Cause: URL is in wrong format."),
    HTTP_EXCEPTION_ERROR(2001, "HTTP exception while sending request!"),
    REQUEST_PARSE_EXCEPTION(2002, "Request cancelled! Cause: Exception while creating request object"),
    CONTEXT_NULL(2003, "Context is null. Init error");

    private String mMessage;
    private int mCode;

    BumerangError(int error, String message) {
        mMessage = message;
        mCode = error;
    }

    public String getMessage() {
        return mMessage;
    }

    public int getCode() {
        return mCode;
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "Error Message: %s , Code: %d", mMessage, mCode);
    }
}
