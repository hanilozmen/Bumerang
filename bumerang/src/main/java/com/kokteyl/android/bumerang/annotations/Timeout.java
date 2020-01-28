package com.kokteyl.android.bumerang.annotations;

import com.kokteyl.android.bumerang.request.Request;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Full url in order to override baseUrl
 * **/
@Target(METHOD)
@Retention(RUNTIME)
public @interface Timeout {
    /**
     * Connect timeout for request
     */
    int connect() default Request.CONNECT_TIMEOUT_DEFAULT;
    /**
     * Read timeout for request
     */
    int read() default Request.READ_TIMEOUT_DEFAULT;

}
